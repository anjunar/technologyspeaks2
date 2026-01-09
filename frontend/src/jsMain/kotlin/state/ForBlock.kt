package state

import core.UiScope
import core.disposeSubtree
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.Node

class ForBlock<T> internal constructor(
    private val parent: Element,
    private val anchor: Node,
    private val renderer: UiScope.(T) -> Unit
) {
    private var mountedNodes: List<Node> = emptyList()

    fun update(items: List<T>) {
        mountedNodes.forEach { n ->
            disposeSubtree(n)
            parent.removeChild(n)
        }
        mountedNodes = emptyList()

        val beforeCount = parent.childNodes.length
        val scope = UiScope(parent)
        for (item in items) scope.renderer(item)
        val afterCount = parent.childNodes.length

        val newNodes = mutableListOf<Node>()
        for (i in beforeCount until afterCount) newNodes += parent.childNodes.item(i)!!

        newNodes.forEach { parent.insertBefore(it, anchor) }
        mountedNodes = newNodes
    }
}

fun <T> UiScope.forEach(items: ReadOnlyProperty<List<T>>, block: UiScope.(T) -> Unit): ForBlock<T> {
    val anchor = document.createComment("for")
    parent.appendChild(anchor)
    val forBlock = ForBlock(parent, anchor, block)
    items.observe { forBlock.update(it) }
    return forBlock
}

