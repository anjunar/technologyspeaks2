package jFx2.core.rendering

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.state.ListProperty
import org.w3c.dom.Element
import org.w3c.dom.Node

private class NodeComponent<N : Node>(override val node: N, override val ui : UiScope) : Component<N>()

fun <T, K> NodeScope.foreach(
    items: List<T>,
    key: (T) -> K,
    factory: NodeScope.(T, Int) -> Component<*>
) {
    val host = create<Element>("div")
    attach(NodeComponent(host, ui))

    val mounts = LinkedHashMap<K, Mount>()

    fun rebuild(newItems: List<T>) {
        val newKeys = newItems.map(key)
        val newMounts = LinkedHashMap<K, Mount>()

        // 1) Unmount removed
        val newKeySet = newKeys.toSet()
        mounts.forEach { (k, m) ->
            if (k !in newKeySet) {
                ui.render.unmount(host, m)
            }
        }

        newItems.forEachIndexed { index, item ->
            val k = key(item)
            val existing = mounts[k]

            val mount = if (existing != null) {
                // reuse
                existing
            } else {
                val outer = this@foreach // funktioniert, weil forEach Extension-Funktion ist
                ui.render.mount(host) {
                    val innerUi = UiScope(
                        dom = outer.ui.dom,
                        build = outer.ui.build,
                        render = outer.ui.render,
                        dispose = this // <- DisposeScope des Mounts
                    )

                    val childScope = NodeScope(
                        ui = innerUi,
                        parent = host,
                        owner = outer.owner,
                        forms = outer.forms
                    )

                    childScope.factory(item, index).node
                }
            }

            newMounts[k] = mount
        }

        mounts.clear()
        mounts.putAll(newMounts)

        var cursor: Node? = null
        for (k in newKeys) {
            val node = mounts[k]!!.node
            if (cursor == null) {
                if (host.firstChild != node) host.insertBefore(node, host.firstChild)
            } else {
                val next = cursor.nextSibling
                if (next != node) host.insertBefore(node, next)
            }
            cursor = node
        }
    }

    rebuild(items)
}

fun <T, K> NodeScope.foreach(
    items: ListProperty<T>,
    key: (T) -> K,
    factory: NodeScope.(T, Int) -> Component<*>
) {
    val host = create<Element>("div")
    attach(NodeComponent(host, ui))

    val mounts = LinkedHashMap<K, Mount>()

    fun rebuild(newItems: List<T>) {
        val newKeys = newItems.map(key)
        val newMounts = LinkedHashMap<K, Mount>()

        val newKeySet = newKeys.toSet()
        mounts.forEach { (k, m) ->
            if (k !in newKeySet) ui.render.unmount(host, m)
        }

        newItems.forEachIndexed { index, item ->
            val k = key(item)
            val existing = mounts[k]

            val mount = if (existing != null) {
                existing
            } else {
                val outer = this@foreach // funktioniert, weil forEach Extension-Funktion ist
                ui.render.mount(host) {
                    val innerUi = UiScope(
                        dom = outer.ui.dom,
                        build = outer.ui.build,
                        render = outer.ui.render,
                        dispose = this // <- DisposeScope des Mounts
                    )

                    val childScope = NodeScope(
                        ui = innerUi,
                        parent = host,
                        owner = outer.owner,
                        forms = outer.forms
                    )

                    childScope.factory(item, index).node
                }
            }

            newMounts[k] = mount
        }

        mounts.clear()
        mounts.putAll(newMounts)

        var cursor: Node? = null
        for (k in newKeys) {
            val node = mounts[k]!!.node
            if (cursor == null) {
                if (host.firstChild != node) host.insertBefore(node, host.firstChild)
            } else {
                val next = cursor.nextSibling
                if (next != node) host.insertBefore(node, next)
            }
            cursor = node
        }
    }

    rebuild(items.get())

    val d = items.observeChanges { _ ->
        rebuild(items.get())
    }
    dispose.register(d)
}

