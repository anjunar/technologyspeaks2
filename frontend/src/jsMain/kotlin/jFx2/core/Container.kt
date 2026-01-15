package jFx2.core

import jFx2.core.capabilities.NodeScope
import jFx2.state.ListChange
import jFx2.state.ListProperty
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node

interface HasChildren {
    val childrenProperty: ListProperty<Component<*>>
}

abstract class Container<C : Node>(node: HTMLElement) : Component<C>(), HasChildren {

    override val childrenProperty: ListProperty<Component<*>> = ListProperty()

    private val mounted = LinkedHashSet<Component<*>>()

    fun bindChildren() {
        reconcile(SetAll(childrenProperty.get()))

        val d = childrenProperty.observeChanges { ch ->
            when (ch) {
                is ListChange.Add -> {
                    ch.items.forEachIndexed { i, child ->
                        insertAt(ch.fromIndex + i, child)
                    }
                }

                is ListChange.Remove -> {
                    ch.items.forEach { child ->
                        removeChild(child)
                    }
                }

                is ListChange.Replace -> {
                    ch.old.forEach { removeChild(it) }
                    ch.new.forEachIndexed { i, child ->
                        insertAt(ch.fromIndex + i, child)
                    }
                }

                is ListChange.Clear -> {
                    mounted.toList().forEach { removeChild(it) }
                }

                is ListChange.SetAll -> {
                    reconcile(SetAll(ch.new))
                }
            }
        }

    }

    private fun insertAt(index: Int, child: Component<*>) {
        if (mounted.contains(child)) return

        val refNode: Node? = node.childNodes.item(index) // null => append
        if (refNode == null) node.appendChild(child.node)
        else node.insertBefore(child.node, refNode)

        mounted.add(child)
    }

    private fun removeChild(child: Component<*>) {
        if (!mounted.contains(child)) return
        if (child.node.parentNode === node) {
            node.removeChild(child.node)
        }
        mounted.remove(child)

    }

    private fun reconcile(setAll: SetAll) {
        val target = setAll.list.toList()

        mounted.toList().asReversed().forEach { c ->
            if (c !in target) removeChild(c)
        }

        target.forEachIndexed { idx, child ->
            if (!mounted.contains(child)) {
                insertAt(idx, child)
            } else {
                val currentNodeAtIdx = node.childNodes.item(idx)
                if (currentNodeAtIdx !== child.node) {
                    insertAt(idx, child)
                }
            }
        }
    }

    private data class SetAll(val list: List<Component<*>>)


}

fun NodeScope.attach(child: Component<*>) {
    val hc = owner as? HasChildren
    if (hc != null && parent === (owner as Component<*>).node) {
        hc.childrenProperty.add(child)
    } else {
        ui.attach(parent, child)
    }
}
