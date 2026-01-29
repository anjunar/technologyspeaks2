package jFx2.core.rendering

import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.runtime.ComponentMount
import jFx2.core.runtime.componentWithScope
import jFx2.state.Disposable
import jFx2.state.Property
import org.w3c.dom.Element

class ConditionBuilder internal constructor() {
    internal var thenBlock: (context(NodeScope) () -> Unit)? = null
    internal var elseBlock: (context(NodeScope) () -> Unit)? = null

    fun then(block: context(NodeScope) () -> Unit) { thenBlock = block }
    fun elseDo(block: context(NodeScope) () -> Unit) { elseBlock = block }
}

private fun mountIntoExistingRange(
    parentScope: NodeScope,
    container: Element,
    owner: RenderContainer,
    block: context(NodeScope) () -> Unit
): ComponentMount {
    val childScope = parentScope.fork(
        parent = container,
        owner = owner,
        ctx = parentScope.ctx.fork(),
        insertPoint = ElementInsertPoint(container)
    )
    return componentWithScope(childScope, block)
}

context(scope: NodeScope)
fun condition(flag: Property<Boolean>, build: ConditionBuilder.() -> Unit) {
    val builder = ConditionBuilder().apply(build)

    val container = attachRenderContainer(scope)
    val containerInsertPoint = ElementInsertPoint(container.node)

    var current: Boolean? = null
    var currentMount: ComponentMount? = null

    fun rebuild(v: Boolean) {
        if (current == v) return
        current = v

        currentMount?.dispose()
        currentMount = null

        containerInsertPoint.clear()

        val chosen = if (v) builder.thenBlock else builder.elseBlock ?: return
        currentMount = mountIntoExistingRange(scope, container.node, container, chosen!!)

        scope.ui.build.afterBuild { container.afterBuild() }
    }

    val d: Disposable = flag.observe { rebuild(it) }
    scope.dispose.register(d)

    rebuild(flag.get())

    scope.dispose.register {
        currentMount?.dispose()
        currentMount = null
        containerInsertPoint.clear()
    }
}

context(scope: NodeScope)
fun condition(flag: () -> Boolean, build: ConditionBuilder.() -> Unit) {
    val builder = ConditionBuilder().apply(build)

    val container = attachRenderContainer(scope)
    val containerInsertPoint = ElementInsertPoint(container.node)

    var current: Boolean? = null
    var currentMount: ComponentMount? = null
    var disposed = false

    fun rebuild(v: Boolean) {
        if (current == v) return
        current = v

        currentMount?.dispose()
        currentMount = null

        containerInsertPoint.clear()

        val chosen = if (v) builder.thenBlock else builder.elseBlock ?: return
        currentMount = mountIntoExistingRange(scope, container.node, container, chosen!!)

        scope.ui.build.afterBuild { container.afterBuild() }
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
        containerInsertPoint.clear()
    }
}
