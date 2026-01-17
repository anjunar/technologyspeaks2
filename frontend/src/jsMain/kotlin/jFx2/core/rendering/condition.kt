package jFx2.core.rendering

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.RangeInsertPoint
import jFx2.core.runtime.ComponentMount
import jFx2.core.runtime.componentWithScope
import jFx2.state.Disposable
import jFx2.state.Property
import kotlinx.browser.document
import org.w3c.dom.Comment
import org.w3c.dom.Element

private class ConditionOwner(override val node: Element) : Component<Element>()

class ConditionBuilder internal constructor() {
    internal var thenBlock: (context(NodeScope) () -> Unit)? = null
    internal var elseBlock: (context(NodeScope) () -> Unit)? = null

    fun then(block: context(NodeScope) () -> Unit) { thenBlock = block }
    fun elseDo(block: context(NodeScope) () -> Unit) { elseBlock = block }
}

private fun mountIntoExistingRange(
    parentScope: NodeScope,
    range: RangeInsertPoint,
    owner: Component<*>,
    block: context(NodeScope) () -> Unit
): ComponentMount {
    val childScope = parentScope.fork(
        parent = range.parent,
        owner = owner,
        ctx = parentScope.ctx.fork(),
        insertPoint = range
    )
    return componentWithScope(childScope, block)
}

context(scope: NodeScope)
fun condition(flag: Property<Boolean>, build: ConditionBuilder.() -> Unit) {
    val builder = ConditionBuilder().apply(build)

    val start: Comment = document.createComment("jFx2:condition")
    val end: Comment = document.createComment("jFx2:/condition")
    scope.insertPoint.insert(start)
    scope.insertPoint.insert(end)

    val range = RangeInsertPoint(start, end)

    val ownerEl = scope.create<Element>("div")
    val owner = ConditionOwner(ownerEl)

    var current: Boolean? = null
    var currentMount: ComponentMount? = null

    fun rebuild(v: Boolean) {
        if (current == v) return
        current = v

        currentMount?.dispose()
        currentMount = null

        range.clear()

        val chosen = if (v) builder.thenBlock else builder.elseBlock ?: return
        currentMount = mountIntoExistingRange(scope, range, owner, chosen!!)
    }

    val d: Disposable = flag.observe { rebuild(it) }
    scope.dispose.register(d)

    rebuild(flag.get())

    scope.dispose.register {
        currentMount?.dispose()
        currentMount = null
        range.dispose()
    }
}

context(scope: NodeScope)
fun condition(flag: () -> Boolean, build: ConditionBuilder.() -> Unit) {
    val builder = ConditionBuilder().apply(build)

    val start: Comment = document.createComment("jFx2:condition")
    val end: Comment = document.createComment("jFx2:/condition")
    scope.insertPoint.insert(start)
    scope.insertPoint.insert(end)

    val range = RangeInsertPoint(start, end)

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

        range.clear()

        val chosen = if (v) builder.thenBlock else builder.elseBlock ?: return
        currentMount = mountIntoExistingRange(scope, range, owner, chosen!!)
    }

    fun scheduleCheck() {
        if (disposed) return

        scope.ui.build.dirty {
            if (disposed) return@dirty
            rebuild(flag())
        }
        scope.ui.build.afterBuild {
            scheduleCheck()
        }
    }

    rebuild(flag())
    scheduleCheck()

    scope.dispose.register {
        disposed = true
        currentMount?.dispose()
        currentMount = null
        range.dispose()
    }
}