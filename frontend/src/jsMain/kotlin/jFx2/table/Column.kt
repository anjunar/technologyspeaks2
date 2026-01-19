package jFx2.table

import jFx2.core.capabilities.NodeScope
import jFx2.state.Disposable
import org.w3c.dom.Element

class Column<R, V>(
    val header: String,
    val prefWidthPx: Int = 160,
    val value: (R) -> V,
    val cellFactory: context(NodeScope) () -> TableCell<R, V>
)

abstract class TableCell<R, V> : Disposable {
    abstract val node: Element

    abstract fun update(
        rowItem: R?,
        rowIndex: Int,
        value: V?,
        empty: Boolean,
        selected: Boolean,
        focused: Boolean
    )
}
