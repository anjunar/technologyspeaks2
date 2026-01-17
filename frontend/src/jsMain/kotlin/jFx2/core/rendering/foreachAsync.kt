package jFx2.core.rendering

import jFx2.core.capabilities.NodeScope
import jFx2.core.runtime.ComponentMount
import jFx2.core.runtime.component
import jFx2.state.Disposable
import jFx2.state.ListChange
import jFx2.state.ListProperty
import jFx2.state.Property
import kotlinx.browser.document
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node

private data class JobItemMount(
    val key: Any,
    val owner: ItemOwner,
    var mount: ComponentMount,
    val index: Property<Int>,
    var job: Job? = null
)

context(scope: NodeScope)
fun <T> foreachAsync(
    items: ListProperty<T>,
    key: (T) -> Any,
    // suspend + context(NodeScope) wie du willst:
    block: suspend context(NodeScope) (T, Property<Int>) -> Unit
) {
    val hostEl = scope.create<Element>("div")
    hostEl.classList.add("foreach-host")
    scope.parent.appendChild(hostEl)

    val mounts = LinkedHashMap<Any, JobItemMount>()

    // CoroutineScope fürs foreach selbst (cancelt alles bei dispose)
    val foreachJob = SupervisorJob()
    val foreachScope = CoroutineScope(foreachJob + Dispatchers.Default)

    fun insertBefore(node: Node, before: Node?) {
        if (before == null) hostEl.appendChild(node) else hostEl.insertBefore(node, before)
    }

    fun disposeAndRemove(im: JobItemMount) {
        im.job?.cancel()
        im.job = null
        im.mount.dispose()
        im.owner.node.parentNode?.removeChild(im.owner.node)
    }

    fun currentDomNodesInOrder(): List<Node> =
        mounts.values.map { it.owner.node }

    fun startAsyncBlock(im: JobItemMount, item: T) {
        // alte job abbrechen (z.B. bei rebuild/reuse)
        im.job?.cancel()

        // childScope pro item – sauberer ctx.fork + owner/parent binden
        val childScope = NodeScope(
            ui = scope.ui,
            parent = im.owner.node,
            owner = im.owner,
            ctx = scope.ctx.fork(),
            dispose = scope.dispose // falls du pro item eigenes dispose willst: hier anpassen
        )

        // Job startet suspend block
        im.job = foreachScope.launch {
            // Wenn dein UI nur auf einem "UI Thread" mutieren darf:
            // dann musst du hier in deine UI-Queue wechseln.
            // Ich nehme an, dein UI hat sowas wie build.enqueue / ui.dispatch / etc.
            // Falls nicht, lass es so und stell sicher, dass block keine illegalen DOM ops macht.
            with(childScope) {
                block(item, im.index)
            }
            // optional: flush nach completion (wenn du das willst)
            // scope.ui.build.flush()
        }

        // Job beim Unmount killen
        scope.dispose.register { im.job?.cancel() }
    }

    fun renderItem(item: T, index: Int): JobItemMount {
        val k = key(item)
        val itemEl = document.createElement("div").unsafeCast<Element>()
        hostEl.appendChild(itemEl)

        val owner = ItemOwner(itemEl)
        val indexProp = Property(index)

        // sync mount: leerer container (oder placeholder)
        val m = component(
            root = itemEl,
            owner = owner,
            ui = scope.ui,
            ctx = scope.ctx.fork()
        ) {
            // optional placeholder
            // text { "Loading..." }
        }

        val im = JobItemMount(k, owner, m, indexProp)
        startAsyncBlock(im, item)
        return im
    }

    fun rebuildSetAll(newItems: List<T>) {
        val newKeys = newItems.map { key(it) }
        val newKeySet = newKeys.toHashSet()

        // remove
        val toRemove = mounts.keys.filter { it !in newKeySet }
        for (k in toRemove) {
            disposeAndRemove(mounts.getValue(k))
            mounts.remove(k)
        }

        // add missing
        newItems.forEachIndexed { index, item ->
            val k = key(item)
            if (!mounts.containsKey(k)) {
                mounts[k] = renderItem(item, index)
            }
        }

        // reorder DOM
        var before: Node? = null
        for (i in newKeys.indices.reversed()) {
            val k = newKeys[i]
            val node = mounts.getValue(k).owner.node
            insertBefore(node, before)
            before = node
        }

        // update indices
        newItems.forEachIndexed { idx, item ->
            mounts[key(item)]?.index?.set(idx)
        }
    }

    // initial
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
                        val indexProp = Property(ch.fromIndex + local)

                        val m = component(
                            root = itemEl,
                            owner = owner,
                            ui = scope.ui,
                            ctx = scope.ctx.fork()
                        ) {
                            // placeholder möglich
                        }

                        val im = JobItemMount(k, owner, m, indexProp)
                        mounts[k] = im
                        startAsyncBlock(im, item)
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
                // new items will be in items.get() anyway
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
        foreachJob.cancel()
        for ((_, im) in mounts) runCatching { disposeAndRemove(im) }
        mounts.clear()
        hostEl.parentNode?.removeChild(hostEl)
    }
}
