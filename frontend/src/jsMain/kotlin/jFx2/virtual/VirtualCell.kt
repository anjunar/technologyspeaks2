package jFx2.virtual

import jFx2.core.capabilities.DisposeScope
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.state.Disposable
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

class VirtualCell<T>(
    val outerScope: NodeScope
) : Disposable {
    val root: HTMLElement = outerScope.ui.dom.create<HTMLDivElement>("div").apply {
        classList.add("jfx-virtual-list-cell")
    }

    private val baseScope: NodeScope = outerScope.fork(
        parent = root,
        owner = outerScope.owner,
        ctx = outerScope.ctx,
        insertPoint = ElementInsertPoint(root)
    )

    private var boundIndex: Int = Int.MIN_VALUE
    private var boundItem: Any? = null
    private var scope: NodeScope? = null

    fun update(index: Int, item: T?, renderer: VirtualListRenderer<T>) {
        if (boundIndex == index && boundItem === item) return
        disposeSubtree()
        boundIndex = index
        boundItem = item
        baseScope.ui.dom.clear(root)

        val cellScope = NodeScope(
            ui = baseScope.ui,
            parent = root,
            owner = baseScope.owner,
            ctx = baseScope.ctx,
            dispose = DisposeScope(),
            insertPoint = ElementInsertPoint(root)
        )
        scope = cellScope

        with(cellScope) {
            renderer(item, index)
            baseScope.ui.build.flush()
        }
    }

    fun recycle() {
        disposeSubtree()
        boundIndex = Int.MIN_VALUE
        boundItem = null
        baseScope.ui.dom.clear(root)
    }

    private fun disposeSubtree() {
        scope?.dispose?.dispose()
        scope = null
    }

    override fun dispose() {
        recycle()
    }
}
