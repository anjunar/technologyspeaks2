package jFx2.table.cells

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.state.CompositeDisposable
import jFx2.state.Disposable
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
        el.classList.toggle("selected", selected)
        el.classList.toggle("focused", focused)
    }

    override fun dispose() {
        // nothing
    }
}

class ComponentCell<R, V>(
    private val scope: NodeScope,
    override val node: Element,                 // host element inside table cell
    private val componentFactory: context(NodeScope) (R, Int, V?) -> Component<*>
) : RecyclableTableCell<R, V>() {

    private val host = node as HTMLElement

    override fun update(
        rowItem: R?,
        rowIndex: Int,
        value: V?,
        empty: Boolean,
        selected: Boolean,
        focused: Boolean
    ) {
        host.classList.toggle("selected", selected)

        if (empty || rowItem == null) {
            resetUpdateBagInternal()
            scope.ui.dom.clear(host)
            return
        }

        resetUpdateBagInternal()
        scope.ui.dom.clear(host)

        val child = componentFactory(scope, rowItem, rowIndex, value)
        val childScope = scope.fork(parent = host, insertPoint = ElementInsertPoint(host    ))
        childScope.attach(child)

        onUpdateDispose { runCatching { child.dispose() } }
    }
}

abstract class RecyclableTableCell<R, V> : TableCell<R, V>() {
    private var updateBag = CompositeDisposable()

    internal fun resetUpdateBagInternal() {
        updateBag.dispose()
        updateBag = CompositeDisposable()
    }

    protected fun onUpdateDispose(d: Disposable) {
        updateBag.add(d)
    }

    override fun dispose() {
        updateBag.dispose()
        onCellDispose()
    }

    protected open fun onCellDispose() {}
}
