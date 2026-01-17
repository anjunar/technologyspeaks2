package jFx2.core.rendering

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.runtime.ComponentMount
import jFx2.core.runtime.component
import jFx2.state.Disposable
import jFx2.state.Property
import kotlinx.browser.document
import org.w3c.dom.Comment
import org.w3c.dom.Element
import org.w3c.dom.Node

private class ConditionOwner(override val node: Element) : Component<Element>()

class ConditionBuilder internal constructor() {
    internal var thenBlock: (context(NodeScope) () -> Unit)? = null
    internal var elseBlock: (context(NodeScope) () -> Unit)? = null

    fun then(block: context(NodeScope) () -> Unit) { thenBlock = block }
    fun elseDo(block: context(NodeScope) () -> Unit) { elseBlock = block }
}

private fun clearBetween(start: Node, end: Node) {
    var n = start.nextSibling
    while (n != null && n !== end) {
        val next = n.nextSibling
        n.parentNode?.removeChild(n)
        n = next
    }
}

private fun mountBetween(
    scope: NodeScope,
    start: Comment,
    end: Comment,
    owner: ConditionOwner,
    block: context(NodeScope) () -> Unit
): ComponentMount {
    val tempRoot = scope.create<Element>("div")

    val tempMount = component(
        root = tempRoot,
        owner = owner,
        ui = scope.ui,
        ctx = scope.ctx.fork(),
        block = block
    )

    val parent = start.parentNode ?: return tempMount
    var child = tempRoot.firstChild
    while (child != null) {
        val next = child.nextSibling
        parent.insertBefore(child, end) // moves node out of tempRoot
        child = next
    }

    return tempMount
}

context(scope: NodeScope)
fun condition(flag: Property<Boolean>, build: ConditionBuilder.() -> Unit) {
    val builder = ConditionBuilder().apply(build)

    val start = document.createComment("jFx2:condition")
    val end   = document.createComment("jFx2:/condition")
    scope.parent.appendChild(start)
    scope.parent.appendChild(end)

    val ownerEl = scope.create<Element>("div")
    val owner = ConditionOwner(ownerEl)

    var current: Boolean? = null
    var currentMount: ComponentMount? = null

    fun rebuild(v: Boolean) {
        if (current == v) return
        current = v

        currentMount?.dispose()
        currentMount = null

        clearBetween(start, end)

        val chosen = if (v) builder.thenBlock else builder.elseBlock ?: return
        currentMount = mountBetween(scope, start, end, owner, chosen!!)
    }

    val d: Disposable = flag.observe { rebuild(it) }
    scope.dispose.register(d)

    rebuild(flag.get())

    scope.dispose.register {
        currentMount?.dispose()
        currentMount = null
        clearBetween(start, end)
        start.parentNode?.removeChild(start)
        end.parentNode?.removeChild(end)
    }
}

context(scope: NodeScope)
fun condition(flag: () -> Boolean, build: ConditionBuilder.() -> Unit) {
    val builder = ConditionBuilder().apply(build)

    val start = document.createComment("jFx2:condition")
    val end   = document.createComment("jFx2:/condition")
    scope.parent.appendChild(start)
    scope.parent.appendChild(end)

    val ownerEl = scope.create<Element>("div")
    val owner = ConditionOwner(ownerEl)

    var current: Boolean? = null
    var currentMount: ComponentMount? = null
    var disposed = false

    fun rebuild(v: Boolean) {
        if (current == v) return
        current = v

        currentMount?.dispose()
        currentMount = null

        clearBetween(start, end)

        val chosen = if (v) builder.thenBlock else builder.elseBlock ?: return
        currentMount = mountBetween(scope, start, end, owner, chosen!!)
    }

    fun scheduleCheck() {
        if (disposed) return
        scope.ui.build.dirty {
            if (disposed) return@dirty
            rebuild(flag())
        }
    }

    rebuild(flag())
    scheduleCheck()

    scope.dispose.register {
        disposed = true
        currentMount?.dispose()
        currentMount = null
        clearBetween(start, end)
        start.parentNode?.removeChild(start)
        end.parentNode?.removeChild(end)
    }
}
