package jFx2.table.dsl

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.table.Column
import jFx2.table.LazyTableModel
import jFx2.table.TableView
import jFx2.table.TableCell
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement

class TableBuilder<R> {
    internal val cols = ArrayList<Column<R, *>>()

    fun <V> column(
        header: String,
        prefWidthPx: Int = 160,
        value: (R) -> V,
        cellFactory: () -> TableCell<R, V>
    ) {
        cols += Column(header, prefWidthPx, value, cellFactory)
    }
}

context(scope: NodeScope)
fun <R> tableView(
    model: LazyTableModel<R>,
    rowHeightPx: Int = 28,
    overscan: Int = 6,
    block: TableBuilder<R>.() -> Unit
): Component<Element> {
    val divElement = scope.create<HTMLDivElement>("div")
    divElement.classList.add("table-view")
    val b = TableBuilder<R>().apply(block)
    return TableView(
        divElement,
        model = model,
        columns = b.cols,
        rowHeightPx = rowHeightPx,
        overscan = overscan
    )
}
