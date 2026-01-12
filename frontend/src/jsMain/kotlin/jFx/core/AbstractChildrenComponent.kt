package jFx.core

import jFx.core.DSL.ElementBuilder
import jFx.state.ListProperty
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node

abstract class AbstractChildrenComponent<E : Node, V: Node> : AbstractComponent<E>(), DSL.ChildNodeBuilder<E,V> {

    override val children: ListProperty<ElementBuilder<*>> = ListProperty(emptyList())

    private val nodeCache = mutableMapOf<ElementBuilder<*>, HTMLElement>()

    override fun afterBuild() {
        children.observe { items ->
            syncChildren(items!!)
        }
        syncChildren(children.get())
    }

    override fun add(child: ElementBuilder<*>) {
        children.setAll(children.get() + child)
        onDispose { safeDisposeChild(child) }
    }

    private fun safeDisposeChild(child: ElementBuilder<*>) {
        try { child.dispose() } catch (_: Throwable) {}
    }

    override fun dispose() {
        children.get().forEach { safeDisposeChild(it) }
        children.clear()
        super<AbstractComponent>.dispose()
    }

    protected fun syncChildren(newItems: List<ElementBuilder<*>>) {
        val currentDomNodes = build().childNodes

        val targetNodes = newItems.map { builder ->
            nodeCache.getOrPut(builder) { builder.build() as HTMLElement }
        }

        targetNodes.forEachIndexed { index, targetNode ->
            val currentNodeAtPosition = currentDomNodes.item(index)

            if (currentNodeAtPosition == null) {
                build().appendChild(targetNode)
            } else if (currentNodeAtPosition != targetNode) {
                build().insertBefore(targetNode, currentNodeAtPosition)
            }
        }

        while (build().childNodes.length > targetNodes.size) {
            build().lastChild?.let { build().removeChild(it) }
        }

        val itemSet = newItems.toSet()
        nodeCache.keys.retainAll(itemSet)
    }

}