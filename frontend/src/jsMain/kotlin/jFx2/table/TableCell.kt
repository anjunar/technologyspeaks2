package jFx2.table.cells

import jFx2.table.TableCell
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

class TextCell<R, V>(
    override val node: Element
) : TableCell<R, V>() {

    override fun update(
        rowItem: R?,
        rowIndex: Int,
        value: V?,
        empty: Boolean,
        selected: Boolean,
        focused: Boolean
    ) {
        val el = node as HTMLElement
        el.textContent = if (empty) "" else (value?.toString() ?: "")
        // optional CSS states:
        el.classList.toggle("selected", selected)
        el.classList.toggle("focused", focused)
    }

    override fun dispose() {
        // nothing
    }
}
