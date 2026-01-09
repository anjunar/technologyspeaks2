package jFx.layout

import jFx.core.AbstractComponent
import jFx.core.DSL.ChildNodeBuilder
import jFx.core.DSL.ElementBuilder
import jFx.state.ListProperty
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

abstract class AbstractBox(val className : String) : AbstractComponent(), ChildNodeBuilder<HTMLDivElement, HTMLElement> {

    private val node by lazy {
        val divElement = document.createElement("div") as HTMLDivElement
        if (className.isNotEmpty()) divElement.classList.add()
        divElement
    }

    override val children: ListProperty<ElementBuilder<*>> = ListProperty(emptyList())

    private val nodeCache = mutableMapOf<ElementBuilder<*>, HTMLElement>()

    init {
        children.observe { items ->
            syncChildren(items)
        }
    }

    override fun build(): HTMLDivElement {
        syncChildren(children.get())
        return node
    }

    override fun add(child: ElementBuilder<*>) {
        children.set(children.get() + child)
    }

    protected fun syncChildren(newItems: List<ElementBuilder<*>>) {
        val currentDomNodes = node.childNodes

        val targetNodes = newItems.map { builder ->
            nodeCache.getOrPut(builder) { builder.build() as HTMLElement }
        }

        targetNodes.forEachIndexed { index, targetNode ->
            val currentNodeAtPosition = currentDomNodes.item(index)

            if (currentNodeAtPosition == null) {
                node.appendChild(targetNode)
            } else if (currentNodeAtPosition != targetNode) {
                node.insertBefore(targetNode, currentNodeAtPosition)
            }
        }

        while (node.childNodes.length > targetNodes.size) {
            node.lastChild?.let { node.removeChild(it) }
        }

        val itemSet = newItems.toSet()
        nodeCache.keys.retainAll(itemSet)
    }

}

