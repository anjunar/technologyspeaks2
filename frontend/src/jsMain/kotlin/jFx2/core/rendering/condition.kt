package jFx2.core.rendering

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.runtime.component
import jFx2.state.Disposable
import jFx2.state.Property
import org.w3c.dom.Element

private class ConditionOwner(override val node: Element) : Component<Element>()

class ConditionBuilder internal constructor() {
    internal var thenBlock: (context(NodeScope) () -> Unit)? = null
    internal var elseBlock: (context(NodeScope) () -> Unit)? = null

    fun then(block: context(NodeScope) () -> Unit) { thenBlock = block }
    fun elseDo(block: context(NodeScope) () -> Unit) { elseBlock = block }
}

context(scope: NodeScope)
fun condition(flag: Property<Boolean>, build: ConditionBuilder.() -> Unit) {
    val builder = ConditionBuilder().apply(build)

    val host = scope.create<Element>("div")
    scope.parent.appendChild(host)

    val owner = ConditionOwner(host)

    var current: Boolean? = null
    var currentMount: jFx2.core.runtime.ComponentMount? = null

    fun rebuild(v: Boolean) {
        if (current == v) return
        current = v

        // dispose previous branch (THIS is the magic you had before)
        currentMount?.dispose()
        currentMount = null

        scope.ui.dom.clear(host)

        val chosen = if (v) builder.thenBlock else builder.elseBlock ?: return

        // new mount for branch
        currentMount = component(
            root = host,
            owner = owner,
            ui = scope.ui,
            ctx = scope.ctx.fork(), // fork context per branch
            block = chosen!!
        )
    }

    val d: Disposable = flag.observe { rebuild(it) }

    // observer also bound to the *current* scope.dispose
    scope.dispose.register(d)

    // initial (falls observe nicht sofort feuert bei dir)
    rebuild(flag.get())
}
