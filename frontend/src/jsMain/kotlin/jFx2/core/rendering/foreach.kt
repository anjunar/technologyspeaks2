package jFx2.core.rendering

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.RangeInsertPoint
import jFx2.core.dom.moveRangeInclusive
import jFx2.core.runtime.ComponentMount
import jFx2.core.runtime.componentWithScope
import jFx2.state.Disposable
import jFx2.state.ListChange
import jFx2.state.ListProperty
import jFx2.state.Property
import kotlinx.browser.document
import org.w3c.dom.Comment
import org.w3c.dom.Element
import org.w3c.dom.Node

private data class RangeItemMount(
    val key: Any,
    val start: Comment,
    val end: Comment,
    val range: RangeInsertPoint,
    val owner: Component<*>,
    var mount: ComponentMount,
    val index: Property<Int>
)

context(scope: NodeScope)
fun <T> foreach(
    items: ListProperty<T>,
    key: (T) -> Any,
    block: context(NodeScope) (T, Property<Int>) -> Unit
) {
    val hostStart = document.createComment("jFx2:foreach")
    val hostEnd = document.createComment("jFx2:/foreach")
    scope.insertPoint.insert(hostStart)
    scope.insertPoint.insert(hostEnd)
    val hostRange = RangeInsertPoint(hostStart, hostEnd)

    val mounts = LinkedHashMap<Any, RangeItemMount>()

    fun disposeAndRemove(im: RangeItemMount) {
        // Ensure markers are always removed even if dispose throws.
        runCatching { im.mount.dispose() }
        runCatching { im.range.dispose() }
    }

    fun mountItem(item: T, idx: Int): RangeItemMount {
        val k = key(item)

        val itemStart = document.createComment("jFx2:item")
        val itemEnd = document.createComment("jFx2:/item")
        hostRange.insert(itemStart)
        hostRange.insert(itemEnd)

        val itemRange = RangeInsertPoint(itemStart, itemEnd)
        val owner: Component<*> = RangeOwner(itemStart)
        val indexProp = Property(idx)

        val childScope = scope.fork(
            parent = itemRange.parent,
            owner = owner,
            ctx = scope.ctx.fork(),
            insertPoint = itemRange
        )

        val m = componentWithScope(childScope) {
            block(item, indexProp)
        }

        return RangeItemMount(k, itemStart, itemEnd, itemRange, owner, m, indexProp)
    }

    fun rebuildSetAll(newItems: List<T>) {
        val newKeys = newItems.map { key(it) }
        val newKeySet = newKeys.toHashSet()

        // Remove missing
        val toRemove = mounts.keys.filter { it !in newKeySet }
        for (k in toRemove) {
            disposeAndRemove(mounts.getValue(k))
            mounts.remove(k)
        }

        // Add missing
        newItems.forEachIndexed { idx, item ->
            val k = key(item)
            if (!mounts.containsKey(k)) {
                mounts[k] = mountItem(item, idx)
            }
        }

        // Reorder marker blocks (stable, wrapper-free)
        val parent: Node = hostStart.parentNode ?: return
        var before: Node? = hostEnd
        for (i in newKeys.indices.reversed()) {
            val k = newKeys[i]
            val im = mounts.getValue(k)
            moveRangeInclusive(parent, im.start, im.end, before)
            before = im.start
        }

        // Update indices
        newItems.forEachIndexed { idx, item ->
            mounts[key(item)]?.index?.set(idx)
        }
    }

    // Initial
    rebuildSetAll(items.get())

    val d: Disposable = items.observeChanges { ch ->
        when (ch) {
            is ListChange.Add,
            is ListChange.Remove,
            is ListChange.Replace,
            is ListChange.SetAll -> rebuildSetAll(items.get())

            is ListChange.Clear -> {
                for ((_, im) in mounts) disposeAndRemove(im)
                mounts.clear()
                hostRange.clear() // leaves foreach markers, clears content between them
            }
        }
    }

    scope.dispose.register(d)
    scope.dispose.register {
        for ((_, im) in mounts) runCatching { disposeAndRemove(im) }
        mounts.clear()
        runCatching { hostRange.dispose() } // removes foreach markers too
    }
}