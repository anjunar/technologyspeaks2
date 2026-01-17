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
    val scope: NodeScope,              // IMPORTANT: stable per-item scope (insertPoint = itemRange)
    var mount: ComponentMount,
    val index: Property<Int>,
    var job: Job? = null,
    var disposed: Boolean = false
)

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

    val mounts = LinkedHashMap<Any, JobRangeItemMount>()

    val foreachJob = SupervisorJob()
    val foreachScope = CoroutineScope(foreachJob + Dispatchers.Default)

    fun disposeAndRemove(im: JobRangeItemMount) {
        im.disposed = true

        runCatching { im.job?.cancel() }
        im.job = null

        // Ensure markers are always removed even if dispose throws.
        runCatching { im.mount.dispose() }
        runCatching { im.range.dispose() }
    }

    fun startAsync(im: JobRangeItemMount, item: T) {
        im.job?.cancel()

        // IMPORTANT: run in the existing stable item scope (no additional fork here!)
        im.job = foreachScope.launch {
            try {
                with(im.scope) {
                    block(item, im.index)
                }
                // Flush once after async render
                im.scope.ui.build.flush()
            } catch (_: CancellationException) {
                // ignore
            } catch (_: Throwable) {
                // if your app wants logging, do it here
            }
        }

        // Safety: cancel on parent dispose
        scope.dispose.register { im.job?.cancel() }
    }

    fun mountItem(item: T, idx: Int): JobRangeItemMount {
        val k = key(item)

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

        // Initial mount (empty placeholder is fine)
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

        // Reorder marker blocks
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