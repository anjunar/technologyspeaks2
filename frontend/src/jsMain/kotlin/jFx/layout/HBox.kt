package jFx.layout

import jFx.core.DSL.ChildNodeBuilder
import jFx.core.DSL.ElementBuilder
import jFx.core.DSL.LifeCycle
import jFx.core.DSL.ParentScope
import jFx.state.ListProperty
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

class HBox : ChildNodeBuilder<HTMLDivElement, HTMLElement> {

    override val children: ListProperty<ElementBuilder<*>> = ListProperty(emptyList())
    override val applyValues: MutableList<() -> Unit> = mutableListOf()
    override var lifeCycle: LifeCycle = LifeCycle.Build

    private val divElement = document.createElement("div") as HTMLDivElement

    private val nodeCache = mutableMapOf<ElementBuilder<*>, HTMLElement>()

    init {
        children.observe { items ->
            syncChildren(items)
        }
    }

    override fun build(): HTMLDivElement {
        syncChildren(children.get())
        return divElement
    }

    override fun add(child: ElementBuilder<*>) {
        children.set(children.get() + child)
    }

    protected fun syncChildren(newItems: List<ElementBuilder<*>>) {
        val currentDomNodes = divElement.childNodes

        val targetNodes = newItems.map { builder ->
            nodeCache.getOrPut(builder) { builder.build() as HTMLElement }
        }

        targetNodes.forEachIndexed { index, targetNode ->
            val currentNodeAtPosition = currentDomNodes.item(index)

            if (currentNodeAtPosition == null) {
                divElement.appendChild(targetNode)
            } else if (currentNodeAtPosition != targetNode) {
                divElement.insertBefore(targetNode, currentNodeAtPosition)
            }
        }

        while (divElement.childNodes.length > targetNodes.size) {
            divElement.lastChild?.let { divElement.removeChild(it) }
        }

        val itemSet = newItems.toSet()
        nodeCache.keys.retainAll(itemSet)
    }

    companion object {
        fun ParentScope.hbox(body: HBox.() -> Unit): HBox {
            val builder = HBox()
            addNode(builder, body)
            return builder
        }
    }
}

