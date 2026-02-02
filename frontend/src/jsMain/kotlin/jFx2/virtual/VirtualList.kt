package jFx2.virtual

import jFx2.core.capabilities.NodeScope
import jFx2.state.Disposable
import jFx2.state.JobRegistry
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.math.max
import kotlin.math.min

typealias VirtualListRenderer<T> = context(NodeScope) (T?, Int) -> Unit

class VirtualList<T>(
    private val outerScope: NodeScope,
    private val viewport: HTMLElement,
    private val dataProvider: RangeDataProvider<T>,
    private val renderer: VirtualListRenderer<T>,
    private val estimateHeightPx: Int = 32,
    private val overscanPx: Int = 200,
    private val prefetchItems: Int = 40,
    private val observeResize: Boolean = true,
    private val coroutineScope: CoroutineScope = JobRegistry.instance.scope
) : Disposable {

    private val ui = outerScope.ui

    private val content: HTMLElement = ui.dom.create("div")
    private val topSpacer: HTMLElement = ui.dom.create("div")
    private val cellsContainer: HTMLElement = ui.dom.create("div")
    private val bottomSpacer: HTMLElement = ui.dom.create("div")

    private val heightCache = HeightCache(estimateHeightPx)
    private val heights = FenwickHeights()

    private var count: Int = 0
    private var renderedStart = 0
    private var renderedEndExclusive = 0
    private var anchorIndex = 0
    private var anchorOffsetPx = 0

    private val activeCells = HashMap<Int, VirtualCell<T>>()
    private val pool = ArrayDeque<VirtualCell<T>>()

    private var layoutScheduled = false
    private var measureScheduled = false
    private var layoutHandle: Int? = null
    private var measureHandle: Int? = null

    private var loading = false
    private var pendingRange: IntRange? = null

    private val scrollListener: (Event) -> Unit = { scheduleLayout() }

    private val resizeObserver: ResizeObserver? =
        if (observeResize && hasResizeObserver()) {
            ResizeObserver { _, _ -> scheduleMeasure() }
        } else {
            null
        }

    init {
        viewport.style.overflowY = "auto"

        content.style.position = "relative"
        cellsContainer.style.position = "relative"

        ui.dom.attach(viewport, content)
        ui.dom.attach(content, topSpacer)
        ui.dom.attach(content, cellsContainer)
        ui.dom.attach(content, bottomSpacer)

        viewport.addEventListener("scroll", scrollListener)
        ensureCountFromProvider()
        scheduleLayout()
    }

    fun invalidate() {
        scheduleLayout()
    }

    override fun dispose() {
        viewport.removeEventListener("scroll", scrollListener)
        layoutHandle?.let { window.cancelAnimationFrame(it) }
        measureHandle?.let { window.cancelAnimationFrame(it) }
        layoutHandle = null
        measureHandle = null

        resizeObserver?.disconnect()

        activeCells.values.forEach { recycleCell(it) }
        activeCells.clear()
        pool.clear()

        ui.dom.detach(content)
    }

    private fun ensureCountFromProvider() {
        val next = when {
            dataProvider.hasKnownCount -> dataProvider.knownCount
            else -> dataProvider.loadedCount
        }.coerceAtLeast(0)

        if (next == count) return
        if (next < count) {
            count = next
            heightCache.trimTo(next)
            heights.rebuildFromHeights(heightCache.heightsSnapshot())
            return
        }

        heightCache.ensureCount(next)
        heights.ensureSize(next)
        for (i in count until next) {
            heights.add(i, heightCache.heightOf(i))
        }
        count = next
    }

    private fun scheduleLayout() {
        if (layoutScheduled) return
        layoutScheduled = true
        layoutHandle = window.requestAnimationFrame {
            layoutScheduled = false
            layoutHandle = null
            doLayout()
        }
    }

    private fun scheduleMeasure() {
        if (measureScheduled) return
        measureScheduled = true
        measureHandle = window.requestAnimationFrame {
            measureScheduled = false
            measureHandle = null
            doMeasure()
        }
    }

    private fun doLayout() {
        ensureCountFromProvider()
        val total = count
        if (total <= 0) {
            updateSpacers(0, 0)
            requestPrefetchForEmpty()
            reconcileCells(0, 0)
            return
        }

        val scrollTop = viewport.scrollTop
        if (scrollTop <= 1.0) {
            anchorIndex = 0
            anchorOffsetPx = 0
        } else {
            anchorIndex = heights.indexAtY(scrollTop)
            anchorOffsetPx = (scrollTop - heights.prefixSum(anchorIndex)).toInt()
        }

        val viewH = viewport.clientHeight.toDouble()
        val startY = max(0.0, scrollTop - overscanPx)
        val endY = scrollTop + viewH + overscanPx

        var startIndex = if (scrollTop <= 1.0) 0 else heights.indexAtY(startY)
        var endIndex = heights.indexAtY(endY) + 1
        if (startIndex < 0) startIndex = 0
        if (endIndex < startIndex) endIndex = startIndex
        if (endIndex > total) endIndex = total

        while (endIndex < total && heights.prefixSum(endIndex) < endY) {
            endIndex++
        }

        renderedStart = startIndex
        renderedEndExclusive = endIndex

        reconcileCells(startIndex, endIndex)
        updateSpacers(startIndex, endIndex)
        requestPrefetch(startIndex, endIndex)
        scheduleMeasure()
    }

    private fun reconcileCells(startIndex: Int, endExclusive: Int) {
        val newCells = HashMap<Int, VirtualCell<T>>(endExclusive - startIndex)

        ui.dom.clear(cellsContainer)

        for (index in startIndex until endExclusive) {
            val existing = activeCells.remove(index)
            val cell = existing ?: obtainCell()
            val item = dataProvider.getOrNull(index)
            cell.update(index, item, renderer)
            newCells[index] = cell

            if (cell.root.parentNode !== cellsContainer) {
                cellsContainer.appendChild(cell.root)
            }

            resizeObserver?.observe(cell.root)
        }

        for ((_, cell) in activeCells) {
            recycleCell(cell)
        }
        activeCells.clear()
        activeCells.putAll(newCells)
    }

    private fun updateSpacers(startIndex: Int, endExclusive: Int) {
        val top = heights.prefixSum(startIndex)
        val bottom = heights.totalSum() - heights.prefixSum(endExclusive)
        topSpacer.style.height = "${max(0, top)}px"
        bottomSpacer.style.height = "${max(0, bottom)}px"
    }

    private fun doMeasure() {
        if (activeCells.isEmpty()) return

        var anchorShift = 0
        var changed = false

        for ((index, cell) in activeCells) {
            val h = cell.root.offsetHeight
            val delta = heightCache.applyMeasuredHeight(index, h)
            if (delta != 0) {
                heights.add(index, delta)
                changed = true
                if (index < anchorIndex) {
                    anchorShift += delta
                }
            }
        }

        if (anchorShift != 0) {
            viewport.scrollTop = viewport.scrollTop + anchorShift
        }

        if (changed) {
            updateSpacers(renderedStart, renderedEndExclusive)
            scheduleLayout()
        }
    }

    private fun requestPrefetch(startIndex: Int, endExclusive: Int) {
        if (prefetchItems <= 0) return
        val from = max(0, startIndex - prefetchItems)
        val to = endExclusive + prefetchItems - 1
        requestEnsureRange(from, to)
    }

    private fun requestPrefetchForEmpty() {
        if (dataProvider.hasKnownCount) return
        if (dataProvider.endReached) return
        val to = max(0, prefetchItems - 1)
        requestEnsureRange(0, to)
    }

    private fun requestEnsureRange(from: Int, to: Int) {
        if (from < 0) return
        var clampedFrom = from
        var clampedTo = to

        if (dataProvider.hasKnownCount) {
            clampedTo = min(clampedTo, dataProvider.knownCount - 1)
        } else if (dataProvider.endReached) {
            clampedTo = min(clampedTo, dataProvider.loadedCount - 1)
        }

        if (clampedTo < clampedFrom) return

        val range = clampedFrom..clampedTo
        if (loading) {
            pendingRange = mergeRanges(pendingRange, range)
            return
        }

        loading = true
        pendingRange = null

        coroutineScope.launch {
            runCatching { dataProvider.ensureRange(range.first, range.last) }
            loading = false
            scheduleLayout()

            val pending = pendingRange
            pendingRange = null
            if (pending != null) {
                requestEnsureRange(pending.first, pending.last)
            }
        }
    }

    private fun mergeRanges(a: IntRange?, b: IntRange): IntRange =
        if (a == null) b else min(a.first, b.first)..max(a.last, b.last)

    private fun obtainCell(): VirtualCell<T> {
        val cell = if (pool.isNotEmpty()) pool.removeFirst() else VirtualCell(outerScope)
        return cell
    }

    private fun recycleCell(cell: VirtualCell<T>) {
        resizeObserver?.unobserve(cell.root)
        ui.dom.detach(cell.root)
        cell.recycle()
        pool.add(cell)
    }

    private fun hasResizeObserver(): Boolean =
        js("typeof ResizeObserver !== 'undefined'") as Boolean
}

context(scope: NodeScope)
fun <T> virtualList(
    viewport: HTMLElement,
    dataProvider: RangeDataProvider<T>,
    renderer: VirtualListRenderer<T>,
    estimateHeightPx: Int = 32,
    overscanPx: Int = 200,
    prefetchItems: Int = 40,
    observeResize: Boolean = true,
    coroutineScope: CoroutineScope = JobRegistry.instance.scope
): VirtualList<T> =
    VirtualList(
        outerScope = scope,
        viewport = viewport,
        dataProvider = dataProvider,
        renderer = renderer,
        estimateHeightPx = estimateHeightPx,
        overscanPx = overscanPx,
        prefetchItems = prefetchItems,
        observeResize = observeResize,
        coroutineScope = coroutineScope
    )

external class ResizeObserver(
    callback: (entries: Array<ResizeObserverEntry>, observer: ResizeObserver) -> Unit
) {
    fun observe(target: Element)
    fun unobserve(target: Element)
    fun disconnect()
}

external interface ResizeObserverEntry {
    val target: Element
}
