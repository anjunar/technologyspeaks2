package jFx2.core.rendering

import jFx2.core.capabilities.NodeScope
import jFx2.core.runtime.ComponentMount
import jFx2.core.runtime.component
import jFx2.state.Disposable
import jFx2.state.ListChange
import jFx2.state.ListProperty
import jFx2.state.Property
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.Node

private data class SimpleItemMount(
    val key: Any,
    val owner: ItemOwner,
    var mount: ComponentMount,
    val index: Property<Int>
)

suspend fun <T> NodeScope.foreachAsync(
    list: List<T>,
    key: (T) -> String,
    block: suspend (T, Int) -> Unit
) {
    for ((i, item) in list.withIndex()) block(item, i)
}

context(scope: NodeScope)
fun <T> foreach(
    items: ListProperty<T>,
    key: (T) -> Any,
    block: context(NodeScope) (T, Property<Int>) -> Unit
) {
    val hostEl = scope.create<Element>("div")
    hostEl.classList.add("foreach-host")
    scope.parent.appendChild(hostEl)

    val mounts = LinkedHashMap<Any, SimpleItemMount>()

    fun renderItem(item: T, index: Int): SimpleItemMount {
        val k = key(item)
        val itemEl = document.createElement("div").unsafeCast<Element>()
        hostEl.appendChild(itemEl)

        val owner = ItemOwner(itemEl)
        val indexProp = jFx2.state.Property(index)

        val m = component(
            root = itemEl,
            owner = owner,
            ui = scope.ui,
            ctx = scope.ctx.fork(),
            block = { block(item, indexProp) }
        )

        return SimpleItemMount(k, owner, m, indexProp)
    }

    fun insertBefore(node: Node, before: Node?) {
        if (before == null) hostEl.appendChild(node) else hostEl.insertBefore(node, before)
    }

    fun disposeAndRemove(im: SimpleItemMount) {
        im.mount.dispose()
        im.owner.node.parentNode?.removeChild(im.owner.node)
    }

    fun currentDomNodesInOrder(): List<Node> =
        mounts.values.map { it.owner.node }

    fun rebuildSetAll(newItems: List<T>) {
        val newKeys = newItems.map { key(it) }

        val newKeySet = newKeys.toHashSet()
        val toRemove = mounts.keys.filter { it !in newKeySet }
        for (k in toRemove) {
            disposeAndRemove(mounts.getValue(k))
            mounts.remove(k)
        }

        newItems.forEachIndexed { index, item ->
            val k = key(item)
            if (!mounts.containsKey(k)) {
                val im = renderItem(item, index)
                mounts[k] = im
            }
        }

        var before: Node? = null
        for (i in newKeys.indices.reversed()) {
            val k = newKeys[i]
            val node = mounts.getValue(k).owner.node
            insertBefore(node, before)
            before = node
        }

        newItems.forEachIndexed { idx, item ->
            val k = key(item)
            mounts[k]?.index?.set(idx)
        }
    }

    rebuildSetAll(items.get())

    val d: Disposable = items.observeChanges { ch ->
        when (ch) {
            is ListChange.Add -> {
                val beforeNode = currentDomNodesInOrder().getOrNull(ch.fromIndex)

                ch.items.forEachIndexed { local, item ->
                    val k = key(item)
                    if (!mounts.containsKey(k)) {
                        val itemEl = document.createElement("div").unsafeCast<Element>()
                        insertBefore(itemEl, beforeNode)

                        val owner = ItemOwner(itemEl)
                        val indexProp = jFx2.state.Property(ch.fromIndex + local)

                        val m = component(
                            root = itemEl,
                            owner = owner,
                            ui = scope.ui,
                            ctx = scope.ctx.fork(),
                            block = { block(item, indexProp) }
                        )
                        mounts[k] = SimpleItemMount(k, owner, m, indexProp)
                    }
                }

                rebuildSetAll(items.get())
            }

            is ListChange.Remove -> {
                for (item in ch.items) {
                    val k = key(item)
                    val im = mounts.remove(k) ?: continue
                    disposeAndRemove(im)
                }
                rebuildSetAll(items.get())
            }

            is ListChange.Replace -> {
                for (item in ch.old) {
                    val k = key(item)
                    val im = mounts.remove(k) ?: continue
                    disposeAndRemove(im)
                }
                rebuildSetAll(items.get())
            }

            is ListChange.Clear -> {
                for ((_, im) in mounts) disposeAndRemove(im)
                mounts.clear()
            }

            is ListChange.SetAll -> rebuildSetAll(ch.new)
        }
    }

    scope.dispose.register(d)
    scope.dispose.register {
        for ((_, im) in mounts) runCatching { disposeAndRemove(im) }
        mounts.clear()
        hostEl.parentNode?.removeChild(hostEl)
    }
}
