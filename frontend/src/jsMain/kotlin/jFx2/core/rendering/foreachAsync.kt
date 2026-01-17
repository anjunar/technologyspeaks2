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
import kotlinx.coroutines.*
import org.w3c.dom.Comment
import org.w3c.dom.Node

private data class JobRangeItemMount(
    val key: Any,
    val start: Comment,
    val end: Comment,
    val range: RangeInsertPoint,
    val owner: Component<*>,
    val scope: NodeScope,              // stable per-item scope
    var mount: ComponentMount,
    val index: Property<Int>,
    var job: Job? = null,
    var disposed: Boolean = false
)

/**
 * Computes indices (in [seq]) of a Longest Increasing Subsequence.
 * Input: seq = positions of existing nodes in the new order (e.g. [5,2,3,7,...])
 * Output: indices in seq that form an LIS (so we keep those "in place").
 */
private fun lisIndices(seq: IntArray): BooleanArray {
    val n = seq.size
    if (n == 0) return BooleanArray(0)

    val prev = IntArray(n) { -1 }
    val tailIdx = IntArray(n)
    val tailVal = IntArray(n)
    var length = 0

    for (i in 0 until n) {
        val x = seq[i]
        // binary search in tailVal[0..length)
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
fun <T> foreachAsync(
    items: ListProperty<T>,
    key: (T) -> Any,
    block: suspend context(NodeScope) (T, Property<Int>) -> Unit
) {
    val hostStart = document.createComment("jFx2:foreach")
    val hostEnd = document.createComment("jFx2:/foreach")
    scope.insertPoint.insert(hostStart)
    scope.insertPoint.insert(hostEnd)
    val hostRange = RangeInsertPoint(hostStart, hostEnd)

    // keyed mounts (stable)
    val mounts = LinkedHashMap<Any, JobRangeItemMount>()

    val foreachJob = SupervisorJob()
    val foreachScope = CoroutineScope(foreachJob + Dispatchers.Default)

    fun disposeAndRemove(im: JobRangeItemMount) {
        im.disposed = true
        runCatching { im.job?.cancel() }
        im.job = null
        runCatching { im.mount.dispose() }
        runCatching { im.range.dispose() }
    }

    fun startAsync(im: JobRangeItemMount, item: T) {
        im.job?.cancel()

        im.job = foreachScope.launch {
            try {
                with(im.scope) { block(item, im.index) }
                im.scope.ui.build.flush()
            } catch (_: CancellationException) {
                // ignore
            } catch (_: Throwable) {
                // optionally log
            }
        }

        // cancel on parent dispose
        scope.dispose.register { im.job?.cancel() }
    }

    fun mountItem(item: T, idx: Int, k: Any): JobRangeItemMount {
        val itemStart = document.createComment("jFx2:item")
        val itemEnd = document.createComment("jFx2:/item")
        hostRange.insert(itemStart)
        hostRange.insert(itemEnd)

        val itemRange = RangeInsertPoint(itemStart, itemEnd)
        val owner: Component<*> = RangeOwner(itemStart)
        val indexProp = Property(idx)

        val itemScope = scope.fork(
            parent = itemRange.parent,
            owner = owner,
            ctx = scope.ctx.fork(),
            insertPoint = itemRange
        )
        itemScope.dispose.register { itemRange.dispose() }

        val m = componentWithScope(itemScope) {
            // optional placeholder
        }

        val im = JobRangeItemMount(
            key = k,
            start = itemStart,
            end = itemEnd,
            range = itemRange,
            owner = owner,
            scope = itemScope,
            mount = m,
            index = indexProp
        )

        startAsync(im, item)
        return im
    }

    /**
     * Core reconcile:
     * - remove missing keys
     * - add new keys (appended first, then moved by reorder phase)
     * - reorder using LIS (minimal moves)
     * - update indices + restart async for items whose content might depend on data
     *
     * This does *not* remount existing items; it keeps scopes/mounts stable.
     */
    fun reconcile(newItems: List<T>, restartJobs: Boolean) {
        val parent: Node = hostStart.parentNode ?: return

        // 1) compute new key order
        val newKeys = ArrayList<Any>(newItems.size)
        for (it in newItems) newKeys += key(it)
        val newKeySet = newKeys.toHashSet()

        // 2) remove missing
        run {
            val iter = mounts.entries.iterator()
            while (iter.hasNext()) {
                val (k, im) = iter.next()
                if (k !in newKeySet) {
                    disposeAndRemove(im)
                    iter.remove()
                }
            }
        }

        // 3) add missing (insert at end before hostEnd for now)
        for (i in newItems.indices) {
            val k = newKeys[i]
            if (!mounts.containsKey(k)) {
                mounts[k] = mountItem(newItems[i], i, k)
            }
        }

        // 4) reorder with LIS
        // Map existing key -> current position in DOM order (based on current keyed order in mounts as they appear)
        // We need a stable "current order list" of keys by DOM. We can derive it by walking newKeys and filtering
        // those that exist, but we must know their current relative order. Easiest: snapshot current DOM order by
        // iterating nodes between hostStart/hostEnd and mapping start markers back to mounts.
        //
        // However: we already own all start markers; we can build current key order by reading mounts in their
        // insertion order *only if* we always keep mounts order synced with DOM. We won't rely on that.
        //
        // We'll compute "current index" by scanning DOM comments between hostStart and hostEnd and matching start nodes.

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

        // Build seq of "current positions" in the order of newKeys, but only for keys that already existed in DOM order.
        // Since we added missing keys by inserting them at end, they will appear at the end in currentKeys; that's fine.
        val seq = IntArray(newKeys.size)
        for (i in newKeys.indices) {
            seq[i] = currentIndex[newKeys[i]] ?: Int.MAX_VALUE // should not happen
        }

        // Determine which elements are part of LIS => keep them, move the others
        val keep = lisIndices(seq)

        // Reorder by walking from end to start; move only those not kept
        var before: Node? = hostEnd
        for (i in newKeys.indices.reversed()) {
            val k = newKeys[i]
            val im = mounts.getValue(k)
            if (!keep[i]) {
                moveRangeInclusive(parent, im.start, im.end, before)
            }
            before = im.start
        }

        // 5) update indices (and optionally restart async)
        for (i in newItems.indices) {
            val k = newKeys[i]
            val im = mounts.getValue(k)
            im.index.set(i)
            if (restartJobs) startAsync(im, newItems[i])
        }

        // 6) keep mounts iteration order somewhat aligned with new order (not required, but good hygiene)
        // rebuild LinkedHashMap in new key order
        if (mounts.size == newKeys.size) {
            val rebuilt = LinkedHashMap<Any, JobRangeItemMount>(mounts.size * 2)
            for (k in newKeys) rebuilt[k] = mounts.getValue(k)
            mounts.clear()
            mounts.putAll(rebuilt)
        }
    }

    // Initial mount: full reconcile + start jobs
    reconcile(items.get(), restartJobs = true)

    // Observe changes:
    // - For pure reorder / add / remove: reconcile and restart jobs only for structural changes if you want.
    // - Most UIs want restart on Replace/SetAll because item instances may have changed behind same key.
    val d: Disposable = items.observeChanges { ch ->
        when (ch) {
            is ListChange.Add -> reconcile(items.get(), restartJobs = false)
            is ListChange.Remove -> reconcile(items.get(), restartJobs = false)

            is ListChange.Replace -> reconcile(items.get(), restartJobs = true)
            is ListChange.SetAll -> reconcile(items.get(), restartJobs = true)

            is ListChange.Clear -> {
                for ((_, im) in mounts) disposeAndRemove(im)
                mounts.clear()
                hostRange.clear()
            }
        }
    }

    scope.dispose.register(d)
    scope.dispose.register {
        foreachJob.cancel()
        for ((_, im) in mounts) runCatching { disposeAndRemove(im) }
        mounts.clear()
        runCatching { hostRange.dispose() }
    }
}
