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
import org.w3c.dom.Node

private data class RangeItemMount(
    val key: Any,
    val start: Comment,
    val end: Comment,
    val range: RangeInsertPoint,
    val owner: Component<*>,
    val scope: NodeScope,              // IMPORTANT: stable per-item scope
    var mount: ComponentMount,
    val index: Property<Int>
)

/**
 * Returns a boolean array "keep" of the same size as seq:
 * keep[i] = true if element i belongs to a Longest Increasing Subsequence.
 *
 * seq must be a sequence of integers; LIS is computed over increasing values.
 */
private fun lisKeepMask(seq: IntArray): BooleanArray {
    val n = seq.size
    if (n == 0) return BooleanArray(0)

    val prev = IntArray(n) { -1 }
    val tailIdx = IntArray(n)
    val tailVal = IntArray(n)
    var length = 0

    for (i in 0 until n) {
        val x = seq[i]

        var lo = 0
        var hi = length
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (tailVal[mid] < x) lo = mid + 1 else hi = mid
        }
        val pos = lo

        if (pos > 0) prev[i] = tailIdx[pos - 1]
        tailIdx[pos] = i
        tailVal[pos] = x
        if (pos == length) length++
    }

    val keep = BooleanArray(n)
    var k = tailIdx[length - 1]
    while (k >= 0) {
        keep[k] = true
        k = prev[k]
    }
    return keep
}

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

    // keyed mounts (stable scopes, stable mounts)
    val mounts = LinkedHashMap<Any, RangeItemMount>()

    fun disposeAndRemove(im: RangeItemMount) {
        runCatching { im.mount.dispose() }
        runCatching { im.range.dispose() }
    }

    fun mountItem(item: T, idx: Int, k: Any): RangeItemMount {
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
        // range cleanup on scope dispose
        childScope.dispose.register { itemRange.dispose() }

        val m = componentWithScope(childScope) {
            block(item, indexProp)
        }

        return RangeItemMount(
            key = k,
            start = itemStart,
            end = itemEnd,
            range = itemRange,
            owner = owner,
            scope = childScope,
            mount = m,
            index = indexProp
        )
    }

    /**
     * Diffing reconcile:
     * - remove missing keys
     * - add new keys (mounted once)
     * - reorder with LIS => minimal DOM moves
     * - update indices
     *
     * NOTE: if your data can change under same key and you need re-render,
     * you can add a "remount/refresh" strategy here (but you asked specifically for diffing).
     */
    fun reconcile(newItems: List<T>) {
        val parent: Node = hostStart.parentNode ?: return

        // 1) new key order
        val newKeys = ArrayList<Any>(newItems.size)
        for (it in newItems) newKeys += key(it)
        val newKeySet = newKeys.toHashSet()

        // 2) remove missing
        run {
            val it = mounts.entries.iterator()
            while (it.hasNext()) {
                val (k, im) = it.next()
                if (k !in newKeySet) {
                    disposeAndRemove(im)
                    it.remove()
                }
            }
        }

        // 3) add missing (inserted at end; reorder step will place correctly)
        for (i in newItems.indices) {
            val k = newKeys[i]
            if (!mounts.containsKey(k)) {
                mounts[k] = mountItem(newItems[i], i, k)
            }
        }

        // 4) compute current DOM order (robust; does not assume mounts iteration == DOM)
        val startToKey = HashMap<Node, Any>(mounts.size * 2)
        for ((k, im) in mounts) startToKey[im.start] = k

        val currentKeys = ArrayList<Any>(mounts.size)
        run {
            var n: Node? = hostStart.nextSibling
            while (n != null && n != hostEnd) {
                val k = startToKey[n]
                if (k != null) currentKeys += k
                n = n.nextSibling
            }
        }

        val currentIndex = HashMap<Any, Int>(currentKeys.size * 2)
        for (i in currentKeys.indices) currentIndex[currentKeys[i]] = i

        // positions of each desired key in current order
        val seq = IntArray(newKeys.size)
        for (i in newKeys.indices) {
            seq[i] = currentIndex[newKeys[i]] ?: Int.MAX_VALUE
        }

        // 5) minimal reorder via LIS
        val keep = lisKeepMask(seq)

        var before: Node? = hostEnd
        for (i in newKeys.indices.reversed()) {
            val k = newKeys[i]
            val im = mounts.getValue(k)
            if (!keep[i]) {
                moveRangeInclusive(parent, im.start, im.end, before)
            }
            before = im.start
        }

        // 6) update indices
        for (i in newItems.indices) {
            mounts.getValue(newKeys[i]).index.set(i)
        }

        // 7) (optional but useful) align mounts iteration order with newKeys
        if (mounts.size == newKeys.size) {
            val rebuilt = LinkedHashMap<Any, RangeItemMount>(mounts.size * 2)
            for (k in newKeys) rebuilt[k] = mounts.getValue(k)
            mounts.clear()
            mounts.putAll(rebuilt)
        }
    }

    // Initial
    reconcile(items.get())

    val d: Disposable = items.observeChanges { ch ->
        when (ch) {
            is ListChange.Add,
            is ListChange.Remove,
            is ListChange.Replace,
            is ListChange.SetAll -> reconcile(items.get())

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
