package jFx2.table.dsl

import jFx2.core.capabilities.NodeScope
import jFx2.table.Column
import jFx2.table.LazyTableModel
import jFx2.table.TableCell
import jFx2.table.TableView

class TableBuilder<R> {
    internal val cols = ArrayList<Column<R, *>>()

    fun <V> column(
        header: String,
        prefWidthPx: Int = 160,
        value: (R) -> V,
        cellFactory: context(NodeScope) () -> TableCell<R, V>
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
): TableView<R> {
    val b = TableBuilder<R>().apply(block)
    val tableView = TableView(
        model = model,
        columns = b.cols,
        rowHeightPx = rowHeightPx,
        overscan = overscan
    )

    scope.attach(tableView)

    return tableView.build()
}
