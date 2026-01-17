package jFx2.core.rendering

import jFx2.core.capabilities.NodeScope
import jFx2.core.runtime.ComponentMount
import jFx2.core.runtime.component
import jFx2.state.Disposable
import jFx2.state.ListChange
import jFx2.state.ListProperty
import jFx2.state.Property
import kotlinx.browser.document
import org.w3c.dom.Node

private data class SimpleItemMount(
    val key: Any,
    val owner: ItemOwner,
    val start: Node,
    val end: Node,
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
    val hostParent = scope.parent
    val hostStart = document.createComment("foreach-host")
    val hostEnd = document.createComment("/foreach-host")
    hostParent.appendChild(hostStart)
    hostParent.appendChild(hostEnd)

    val mounts = LinkedHashMap<Any, SimpleItemMount>()

    fun renderItem(item: T, index: Int): SimpleItemMount {
        val k = key(item)
        val itemStart = document.createComment("foreach-item")
        val itemEnd = document.createComment("/foreach-item")
        hostParent.insertBefore(itemStart, hostEnd)
        hostParent.insertBefore(itemEnd, hostEnd)

        val owner = ItemOwner(itemStart)
        val indexProp = jFx2.state.Property(index)

        val m = component(
            root = itemStart,
            owner = owner,
            ui = scope.ui,
            ctx = scope.ctx.fork(),
            parent = hostParent,
            anchor = itemEnd,
            block = { block(item, indexProp) }
        )

        return SimpleItemMount(k, owner, itemStart, itemEnd, m, indexProp)
    }

    fun moveRangeBefore(start: Node, end: Node, before: Node?) {
        val parent = start.parentNode ?: return
        val fragment = document.createDocumentFragment()
        var current: Node? = start
        while (current != null) {
            val next = current.nextSibling
            fragment.appendChild(current)
            if (current == end) break
            current = next
        }
        parent.insertBefore(fragment, before)
    }

    fun disposeAndRemove(im: SimpleItemMount) {
        im.mount.dispose()
        val parent = im.start.parentNode ?: return
        var current: Node? = im.start
        while (current != null) {
            val next = current.nextSibling
            parent.removeChild(current)
            if (current == im.end) break
            current = next
        }
    }

    fun currentDomNodesInOrder(): List<Node> =
        mounts.values.map { it.start }

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

        var before: Node? = hostEnd
        for (i in newKeys.indices.reversed()) {
            val k = newKeys[i]
            val im = mounts.getValue(k)
            moveRangeBefore(im.start, im.end, before)
            before = im.start
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
                        val itemStart = document.createComment("foreach-item")
                        val itemEnd = document.createComment("/foreach-item")
                        hostParent.insertBefore(itemStart, beforeNode ?: hostEnd)
                        hostParent.insertBefore(itemEnd, beforeNode ?: hostEnd)

                        val owner = ItemOwner(itemStart)
                        val indexProp = jFx2.state.Property(ch.fromIndex + local)

                        val m = component(
                            root = itemStart,
                            owner = owner,
                            ui = scope.ui,
                            ctx = scope.ctx.fork(),
                            parent = hostParent,
                            anchor = itemEnd,
                            block = { block(item, indexProp) }
                        )
                        mounts[k] = SimpleItemMount(k, owner, itemStart, itemEnd, m, indexProp)
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
        hostStart.parentNode?.removeChild(hostStart)
        hostEnd.parentNode?.removeChild(hostEnd)
    }
}
