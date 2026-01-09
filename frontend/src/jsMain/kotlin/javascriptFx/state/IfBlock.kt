package javascriptFx.state

import javascriptFx.core.Component
import javascriptFx.core.UiScope
import javascriptFx.core.disposeSubtree
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.Node

class IfBlock internal constructor(
    private val parent: Component<*>,
    private val anchor: Node,
    private val content: UiScope.() -> Unit
) {
    private var mountedNodes: List<Node> = emptyList()

    private fun mount() {
        if (mountedNodes.isNotEmpty()) return

        val beforeCount = parent.node.childNodes.length
        UiScope(parent).content()
        val afterCount = parent.node.childNodes.length

        val newNodes = mutableListOf<Node>()
        for (i in beforeCount until afterCount) newNodes += parent.node.childNodes.item(i)!!

        newNodes.forEach { parent.node.insertBefore(it, anchor) }
        mountedNodes = newNodes
    }

    private fun unmount() {
        mountedNodes.forEach { n ->
            disposeSubtree(n)
            parent.node.removeChild(n)
        }
        mountedNodes = emptyList()
    }

    fun set(visible: Boolean) {
        if (visible) mount() else unmount()
    }
}

fun UiScope.ifBlock(condition: ReadOnlyProperty<Boolean>, block: UiScope.() -> Unit): IfBlock {
    val anchor = document.createComment("if")
    parent.node.appendChild(anchor)
    val ifBlock = IfBlock(parent, anchor, block)
    condition.observe { ifBlock.set(it) }
    return ifBlock
}

