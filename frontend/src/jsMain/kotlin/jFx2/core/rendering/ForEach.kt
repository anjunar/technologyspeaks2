package jFx2.core.rendering

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.state.ListProperty
import org.w3c.dom.Element
import org.w3c.dom.Node

private class NodeComponent<N : Node>(override val node: N) : Component<N>()

fun <T, K> NodeScope.forEach(
    items: List<T>,
    key: (T) -> K,
    factory: NodeScope.(T) -> Component<*>
) {
    val host = create<Element>("div")
    attach(NodeComponent(host))

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

        for (item in newItems) {
            val k = key(item)
            val existing = mounts[k]

            val mount = if (existing != null) {
                // reuse
                existing
            } else {
                ui.render.mount(host) {
                    val childScope = NodeScope(ui, host)
                    childScope.factory(item).node
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

fun <T, K> NodeScope.forEach(
    items: ListProperty<T>,
    key: (T) -> K,
    factory: NodeScope.(T) -> Component<*>
) {
    val host = create<Element>("div")
    attach(NodeComponent(host))

    val mounts = LinkedHashMap<K, Mount>()

    fun rebuild(newItems: List<T>) {
        val newKeys = newItems.map(key)
        val newMounts = LinkedHashMap<K, Mount>()

        val newKeySet = newKeys.toSet()
        mounts.forEach { (k, m) ->
            if (k !in newKeySet) ui.render.unmount(host, m)
        }

        for (item in newItems) {
            val k = key(item)
            val existing = mounts[k]

            val mount = if (existing != null) {
                existing
            } else {
                ui.render.mount(host) {
                    val childScope = NodeScope(ui, host)
                    childScope.factory(item).node
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

