package jFx2.table

import jFx2.state.Property
import jFx2.state.Disposable
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

internal class VirtualTableFlow<R>(
    private val viewport: HTMLElement,
    private val content: HTMLElement,
    private val rowHeightPx: Int,
    private val overscan: Int,
    private val columns: List<Column<R, *>>,
    private val model: LazyTableModel<R>,
    private val selectedIndex: Property<Int?>,
    private val focusedIndex: Property<Int?>
) : Disposable {

    data class CellHolder<R, V>(
        val col: Column<R, V>,
        val cell: TableCell<R, V>,
        val host: HTMLElement
    ) : Disposable {
        override fun dispose() = cell.dispose()
    }

    class VirtualRow<R>(
        val node: HTMLElement,
        val cells: List<CellHolder<R, *>>
    ) : Disposable {
        var boundIndex: Int = -1
        fun setTopPx(px: Int) { node.style.top = "${px}px" }
        override fun dispose() { cells.forEach { it.dispose() } }
    }

    private val rows = ArrayList<VirtualRow<R>>()

    private val scrollListener: (Event) -> Unit = { render() }

    private val dInvalidate = model.invalidateTick.observe { render() }
    private val dCount = model.totalCount.observe { updateContentHeight(); render() }
    private val dSel = selectedIndex.observe { render() }
    private val dFocus = focusedIndex.observe { render() }

    init {
        viewport.addEventListener("scroll", scrollListener)
        updateContentHeight()
        render()
    }

    override fun dispose() {
        viewport.removeEventListener("scroll", scrollListener)
        dInvalidate.dispose()
        dCount.dispose()
        dSel.dispose()
        dFocus.dispose()
        rows.forEach { it.dispose() }
        rows.clear()
    }

    private fun updateContentHeight() {
        val total = model.totalCount.get()
        if (total != null) {
            content.style.height = "${total * rowHeightPx}px"
        } else {
            // unknown size: start with a baseline; we grow when needed
            content.style.height = "${1000 * rowHeightPx}px"
        }
    }

    private fun ensureRowPoolSize(needed: Int) {
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
                val host = (content.ownerDocument!!.createElement("div") as HTMLElement).apply {
                    className = "jfx-table-cell-host"
                    style.flex = "0 0 ${col.prefWidthPx}px"
                    style.overflowX = "hidden"
                    style.overflowY = "hidden"
                    style.whiteSpace = "nowrap"
                    style.textOverflow = "ellipsis"
                    style.padding = "0 8px"
                    style.display = "flex"
                    style.alignItems = "center"
                }
                rowEl.appendChild(host)

                error("ensureRowPoolSize must be called from TableView where cells are created with NodeScope")
            }

            // unreachable due to error() above
        }
    }

    /**
     * Called by TableView after it created the row pool with real cells.
     */
    fun setRows(pool: List<VirtualRow<R>>) {
        rows.clear()
        rows.addAll(pool)
        render()
    }

    fun render() {
        val viewportH = viewport.clientHeight
        val scrollTop = viewport.scrollTop.toDouble()

        val totalKnown = model.totalCount.get()
        val first = max(0, floor(scrollTop / rowHeightPx).toInt() - overscan)
        val visible = ceil(viewportH.toDouble() / rowHeightPx).toInt() + overscan * 2
        val lastExclusive = first + visible

        val start: Int
        val endExclusive: Int
        if (totalKnown != null) {
            start = min(first, max(0, totalKnown - 1))
            endExclusive = min(lastExclusive, totalKnown)
        } else {
            start = first
            endExclusive = lastExclusive

            // grow content if needed
            val requiredHeight = endExclusive * rowHeightPx
            if (requiredHeight > content.offsetHeight) {
                content.style.height = "${requiredHeight + (rowHeightPx * 500)}px"
            }
        }

        val count = max(0, endExclusive - start)
        if (count == 0 || rows.isEmpty()) return

        val used = min(count, rows.size)

        for (slot in 0 until used) {
            val index = start + slot
            val row = rows[slot]
            row.boundIndex = index
            row.setTopPx(index * rowHeightPx)

            val item = model.get(index)
            val empty = item == null

            val selected = selectedIndex.get() == index
            val focused = focusedIndex.get() == index

            row.node.classList.toggle("selected", selected)

            row.cells.forEach { holderAny ->
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

            row.node.style.display = "flex"
        }

        for (i in used until rows.size) {
            rows[i].node.style.display = "none"
            rows[i].boundIndex = -1
        }
    }

    companion object {
        fun <R, V> cellHolder(col: Column<R, V>, cell: TableCell<R, V>, host: HTMLElement) =
            CellHolder(col, cell, host)

        fun <R> virtualRow(node: HTMLElement, cells: List<CellHolder<R, *>>) =
            VirtualRow(node, cells)
    }
}
