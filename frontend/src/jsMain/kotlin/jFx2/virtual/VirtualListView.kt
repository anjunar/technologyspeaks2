package jFx2.virtual

import jFx2.core.Component
import jFx2.core.capabilities.DisposeScope
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.state.Disposable
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.Event
import kotlin.math.max
import kotlin.math.min

class VirtualListView<T>(
    private val dataProvider: RangeDataProvider<T>,
    private val estimateHeightPx: Int = 44,
    private val overscanPx: Int = 240,
    private val prefetchItems: Int = 80,
    private val renderer: context(NodeScope) (item: T?, index: Int) -> Unit
) : Component<HTMLDivElement>() {

    override val node: HTMLDivElement = document.createElement("div") as HTMLDivElement

    private lateinit var uiScope: UiScope
    private lateinit var baseScope: NodeScope
    private lateinit var viewport: HTMLDivElement
    private lateinit var content: HTMLDivElement

    private val job = SupervisorJob()
    private val cs = CoroutineScope(job)

    private var loadingJob: Job? = null
    private var pendingTarget = -1
    private var tailPaddingItems = prefetchItems * 3

    private val heights = ArrayList<Int>()
    private val prefix = ArrayList<Int>().apply { add(0) }

    private data class Slot(
        val node: HTMLDivElement,
        var boundIndex: Int = -1,
        var loaded: Boolean = false,
        var dispose: DisposeScope? = null
    )

    private val slots = ArrayList<Slot>()

    private var renderScheduled = false
    private var measureScheduled = false

    init {
        node.className = "jfx-virtual-list"
        onDispose(Disposable { job.cancel() })
        onDispose(Disposable { slots.forEach { it.dispose?.dispose() } })
    }

    context(scope: NodeScope)
    fun build(): VirtualListView<T> {
        uiScope = scope.ui
        baseScope = scope
        uiScope.dom.clear(node)

        viewport = scope.create<HTMLDivElement>("div").apply {
            className = "jfx-virtual-list-viewport"
            tabIndex = 0
        }

        content = scope.create<HTMLDivElement>("div").apply {
            className = "jfx-virtual-list-content"
        }

        viewport.appendChild(content)
        node.appendChild(viewport)

        viewport.addEventListener("scroll", onScroll)
        onDispose(Disposable { viewport.removeEventListener("scroll", onScroll) })

        val resize = windowResizeListener()
        onDispose(resize)

        scheduleRender()
        requestRange(prefetchItems)

        return this
    }

    override fun dispose() {
        super.dispose()
        loadingJob = null
    }

    private val onScroll: (Event) -> Unit = { scheduleRender() }

    private fun windowResizeListener(): Disposable {
        val listener: (Event) -> Unit = { scheduleRender() }
        window.addEventListener("resize", listener)
        return Disposable { window.removeEventListener("resize", listener) }
    }

    private fun scheduleRender() {
        if (renderScheduled) return
        renderScheduled = true
        window.requestAnimationFrame {
            renderScheduled = false
            render()
        }
    }

    private fun scheduleMeasure() {
        if (measureScheduled) return
        measureScheduled = true
        window.requestAnimationFrame {
            measureScheduled = false
            var changed = false
            slots.forEach { slot ->
                val idx = slot.boundIndex
                if (idx >= 0 && slot.loaded) {
                    val h = slot.node.offsetHeight
                    if (h > 0 && updateHeight(idx, h)) {
                        changed = true
                    }
                }
            }
            if (changed) {
                updateContentHeight()
                render()
            }
        }
    }

    private fun ensureHeightsSize(size: Int) {
        while (heights.size < size) {
            heights.add(estimateHeightPx)
            prefix.add(prefix.last() + estimateHeightPx)
        }
    }

    private fun updateHeight(index: Int, newHeight: Int): Boolean {
        if (index < 0) return false
        ensureHeightsSize(index + 1)
        val old = heights[index]
        if (old == newHeight) return false
        heights[index] = newHeight
        val delta = newHeight - old
        for (i in index + 1 until prefix.size) {
            prefix[i] = prefix[i] + delta
        }
        return true
    }

    private fun offsetFor(index: Int): Int {
        val loaded = heights.size
        return if (index <= loaded) {
            prefix[index]
        } else {
            prefix.last() + (index - loaded) * estimateHeightPx
        }
    }

    private fun indexForOffset(offset: Int): Int {
        val off = max(0, offset)
        val loaded = heights.size
        if (loaded == 0) return 0
        val total = prefix.last()
        if (off >= total) {
            return loaded + ((off - total) / estimateHeightPx)
        }
        var lo = 0
        var hi = loaded
        while (lo < hi) {
            val mid = (lo + hi) / 2
            if (prefix[mid + 1] <= off) {
                lo = mid + 1
            } else {
                hi = mid
            }
        }
        return lo
    }

    private fun heightFor(index: Int): Int =
        if (index in heights.indices) heights[index] else estimateHeightPx

    private fun updateContentHeight() {
        val known = if (dataProvider.hasKnownCount) dataProvider.knownCount else null
        val base = prefix.last()
        val extra = when {
            dataProvider.endReached -> 0
            known != null -> max(0, known - heights.size) * estimateHeightPx
            else -> tailPaddingItems * estimateHeightPx
        }
        content.style.height = "${base + extra}px"
    }

    private fun ensureSlotCount(needed: Int) {
        while (slots.size < needed) {
            val node = baseScope.create<HTMLDivElement>("div").apply {
                className = "jfx-virtual-list-cell"
            }
            content.appendChild(node)
            slots.add(Slot(node))
        }
    }

    private fun renderSlot(slot: Slot, item: T?, index: Int) {
        slot.dispose?.dispose()
        uiScope.dom.clear(slot.node)
        val dispose = DisposeScope()
        slot.dispose = dispose

        val slotScope = NodeScope(
            ui = uiScope,
            parent = slot.node,
            owner = this,
            ctx = baseScope.ctx,
            dispose = dispose,
            insertPoint = ElementInsertPoint(slot.node)
        )

        with(slotScope) {
            renderer(item, index)
            uiScope.build.flush()
        }
    }

    private fun requestRange(targetInclusiveRaw: Int) {
        if (dataProvider.endReached) return
        val known = if (dataProvider.hasKnownCount) dataProvider.knownCount else null
        val targetInclusive = if (known != null) min(targetInclusiveRaw, known - 1) else targetInclusiveRaw
        if (targetInclusive < 0) return
        if (targetInclusive < dataProvider.loadedCount) return

        if (loadingJob?.isActive == true) {
            pendingTarget = max(pendingTarget, targetInclusive)
            return
        }

        loadingJob = cs.launch {
            dataProvider.ensureRange(0, targetInclusive)
            ensureHeightsSize(dataProvider.loadedCount)
            if (dataProvider.endReached) {
                tailPaddingItems = 0
            }
            updateContentHeight()
            scheduleRender()
            val pending = pendingTarget
            pendingTarget = -1
            if (pending >= 0) requestRange(pending)
        }
    }

    private fun render() {
        val viewportH = viewport.clientHeight
        if (viewportH <= 0) return

        ensureHeightsSize(dataProvider.loadedCount)
        updateContentHeight()

        val scrollTop = viewport.scrollTop.toInt()
        val startOffset = max(0, scrollTop - overscanPx)
        val endOffset = scrollTop + viewportH + overscanPx

        val known = if (dataProvider.hasKnownCount) dataProvider.knownCount else null
        val maxCount = when {
            known != null -> known
            dataProvider.endReached -> dataProvider.loadedCount
            else -> Int.MAX_VALUE
        }

        var index = indexForOffset(startOffset)
        var top = offsetFor(index)
        val visible = ArrayList<Pair<Int, Int>>()

        while (top < endOffset && index < maxCount) {
            visible.add(index to top)
            top += heightFor(index)
            index += 1
            if (visible.size > 1200) break
        }

        if (visible.isEmpty()) return

        val needed = visible.size
        ensureSlotCount(needed)

        for (i in 0 until slots.size) {
            val slot = slots[i]
            if (i < needed) {
                val (idx, topPx) = visible[i]
                slot.node.style.top = "${topPx}px"
                slot.node.classList.remove("is-hidden")

                val item = dataProvider.getOrNull(idx)
                val loaded = item != null
                if (slot.boundIndex != idx || slot.loaded != loaded) {
                    renderSlot(slot, item, idx)
                }
                slot.boundIndex = idx
                slot.loaded = loaded
            } else {
                slot.node.classList.add("is-hidden")
                slot.boundIndex = -1
                slot.loaded = false
            }
        }

        if (!dataProvider.endReached && maxCount > 0) {
            val target = min(index + prefetchItems, maxCount - 1)
            if (known == null) {
                if (target > heights.size + tailPaddingItems - 1) {
                    tailPaddingItems += prefetchItems * 2
                    updateContentHeight()
                }
            }
            requestRange(target)
        }

        scheduleMeasure()
    }
}
