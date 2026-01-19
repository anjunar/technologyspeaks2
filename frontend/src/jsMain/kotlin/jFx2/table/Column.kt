package jFx2.table

import jFx2.state.Disposable
import org.w3c.dom.Element

/**
 * Column definition (like TableColumn<S, T>).
 *
 * value:  extract a value from row item
 * cellFactory: create a reusable cell instance
 */
class Column<R, V>(
    val header: String,
    val prefWidthPx: Int = 160,
    val value: (R) -> V,
    val cellFactory: () -> TableCell<R, V>
)

abstract class TableCell<R, V> : Disposable {
    abstract val node: Element

    /**
     * Like JavaFX updateItem(item, empty) but with additional signals.
     */
    abstract fun update(
        rowItem: R?,
        rowIndex: Int,
        value: V?,
        empty: Boolean,
        selected: Boolean,
        focused: Boolean
    )
}
