package jFx2.core.rendering

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.state.Disposable
import jFx2.state.ListChange
import jFx2.state.ListProperty
import org.w3c.dom.Element

private class ForEachMount(override val node: Element) : Component<Element>()

context(scope: NodeScope)
fun <T> foreach(
    items: ListProperty<T>,
    key: (T) -> Any = { it as Any },
    block: context(NodeScope) (T, Int) -> Unit
) {
    val host = scope.create<Element>("div")
    scope.parent.appendChild(host)

    // Track mounts by key (stable diff)
    val mounts = LinkedHashMap<Any, ForEachMount>()

    fun renderAll(newItems: List<T>) {
        // dispose everything
        for ((_, m) in mounts) m.dispose()
        mounts.clear()
        scope.ui.dom.clear(host)

        newItems.forEachIndexed { i, item ->
            val k = key(item)
            val el = scope.ui.dom.create<Element>("div")
            host.appendChild(el)
            val m = ForEachMount(el)
            mounts[k] = m

            val childScope = NodeScope(ui = scope.ui, parent = el, owner = m, ctx = scope.ctx.fork(), scope.dispose)
            block(childScope, item, i)
        }
        scope.ui.build.flush()
    }

    renderAll(items.get())

    val d: Disposable = items.observeChanges { ch ->
        // Minimal strategy (safe): full rerender.
        // You can optimize later with incremental Add/Remove/Replace.
        when (ch) {
            is ListChange.Add,
            is ListChange.Remove,
            is ListChange.Replace,
            is ListChange.Clear,
            is ListChange.SetAll -> renderAll(items.get())
        }
    }

    scope.ui.dispose.register(d)
}
