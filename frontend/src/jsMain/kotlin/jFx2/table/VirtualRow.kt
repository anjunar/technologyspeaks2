package jFx2.table

import jFx2.state.Disposable
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

internal class VirtualRow<R>(
    val node: HTMLElement,
    val cellHolders: List<CellHolder<R, *>>
) : Disposable {
    var boundIndex: Int = -1

    fun setTopPx(px: Int) {
        node.style.top = "${px}px"
    }

    override fun dispose() {
        cellHolders.forEach { it.dispose() }
    }
}

internal class CellHolder<R, V>(
    val col: Column<R, V>,
    val cell: TableCell<R, V>,
    val node: HTMLElement
) : Disposable {
    override fun dispose() = cell.dispose()
}

/**
 * Virtualized flow:
 *  - viewport: overflow:auto; position:relative;
 *  - content: position:relative; height: totalRows*rowHeight
 *  - rows: position:absolute; top = index*rowHeight
 */
internal class VirtualTableFlow<R>(
    private val viewport: HTMLElement,
    private val content: HTMLElement,
    private val rowHeightPx: Int,
    private val overscan: Int,
    private val columns: List<Column<R, *>>,
    private val model: LazyTableModel<R>,
    private val selectionIndex: jFx2.state.Property<Int?>,
    private val focusedIndex: jFx2.state.Property<Int?>
) : Disposable {

    private val rows = ArrayList<VirtualRow<R>>()
    private var lastFirst = -1
    private var lastCount = -1

    private val scrollListener: (Event) -> Unit = { render() }

    init {
        viewport.addEventListener("scroll", scrollListener)

        // Re-render when model loads something
        model.invalidateTick.observe { render() } // adapt subscribe API if different
        model.totalCount.observe { updateContentHeight(); render() }
        selectionIndex.observe { render() }
        focusedIndex.observe { render() }

        updateContentHeight()
        render()
    }

    override fun dispose() {
        viewport.removeEventListener("scroll", scrollListener)
        rows.forEach { it.dispose() }
        rows.clear()
    }

    private fun updateContentHeight() {
        val total = model.totalCount.value
        if (total != null) {
            content.style.height = "${total * rowHeightPx}px"
        } else {
            // unknown: let it grow heuristically as user scrolls; start with something.
            val minRows = 1000
            content.style.height = "${minRows * rowHeightPx}px"
        }
    }

    private fun ensureRowPoolSize(needed: Int) {
        if (rows.size == needed) return
        // grow-only in MVP (shrink not necessary)
        while (rows.size < needed) {
            val rowEl = (content.ownerDocument!!.createElement("div") as HTMLElement).apply {
                className = "jfx-table-row"
                style.position = "absolute"
                style.left = "0px"
                style.right = "0px"
                style.height = "${rowHeightPx}px"
                style.display = "flex"
            }

            val holders = columns.map { col ->
                // create cell DOM
                val cellNode = (content.ownerDocument!!.createElement("div") as HTMLElement).apply {
                    className = "jfx-table-cell"
                    style.flex = "0 0 ${col.prefWidthPx}px"
                    style.overflowX = "hidden"
                    style.overflowY = "hidden"
                    style.whiteSpace = "nowrap"
                    style.textOverflow = "ellipsis"
                    style.padding = "0 8px"
                    style.display = "flex"
                    style.alignItems = "center"
                }
                rowEl.appendChild(cellNode)

                // create cell instance via factory
                @Suppress("UNCHECKED_CAST")
                val typedCol = col as Column<R, Any?>
                @Suppress("UNCHECKED_CAST")
                val cell = typedCol.cellFactory() as TableCell<R, Any?>

                // mount cell.node into cellNode:
                cellNode.appendChild(cell.node)

                CellHolder(typedCol, cell, cellNode)
            }

            // click -> select
            rowEl.addEventListener("click", { _ ->
                val idx = rows.firstOrNull { it.node === rowEl }?.boundIndex ?: -1
                if (idx >= 0) selectionIndex.value = idx
            })

            content.appendChild(rowEl)
            rows += VirtualRow(rowEl, holders)
        }
    }

    fun render() {
        val viewportH = viewport.clientHeight
        val scrollTop = viewport.scrollTop.toDouble()

        val totalKnown = model.totalCount.value
        val first = max(0, floor(scrollTop / rowHeightPx).toInt() - overscan)
        val visible = ceil(viewportH.toDouble() / rowHeightPx).toInt() + overscan * 2
        val lastExclusive = first + visible

        val clampedFirst: Int
        val clampedLastExclusive: Int
        if (totalKnown != null) {
            clampedFirst = min(first, max(0, totalKnown - 1))
            clampedLastExclusive = min(lastExclusive, totalKnown)
        } else {
            clampedFirst = first
            clampedLastExclusive = lastExclusive
            // grow content height if user scrolls near the bottom
            val requiredHeight = clampedLastExclusive * rowHeightPx
            val currentHeight = content.offsetHeight
            if (requiredHeight > currentHeight) {
                content.style.height = "${requiredHeight + (rowHeightPx * 500)}px"
            }
        }

        val count = max(0, clampedLastExclusive - clampedFirst)
        if (count == 0) return

        if (clampedFirst == lastFirst && count == lastCount) {
            // still update because data might have loaded
        } else {
            lastFirst = clampedFirst
            lastCount = count
            ensureRowPoolSize(count)
        }

        for (slot in 0 until count) {
            val index = clampedFirst + slot
            val row = rows[slot]
            row.boundIndex = index
            row.setTopPx(index * rowHeightPx)

            val item = model.get(index)
            val empty = item == null

            val selected = selectionIndex.value == index
            val focused = focusedIndex.value == index

            // update each cell
            row.cellHolders.forEach { holderAny ->
                @Suppress("UNCHECKED_CAST")
                val holder = holderAny as CellHolder<R, Any?>
                val v = if (!empty) holder.col.value(item as R) else null
                holder.cell.update(
                    rowItem = item,
                    rowIndex = index,
                    value = v,
                    empty = empty,
                    selected = selected,
                    focused = focused
                )
            }

            row.node.classList.toggle("selected", selected)
        }

        // hide unused pooled rows (if pool > count)
        for (i in count until rows.size) {
            rows[i].node.style.display = "none"
            rows[i].boundIndex = -1
        }
        for (i in 0 until count) {
            rows[i].node.style.display = "flex"
        }
    }
}
