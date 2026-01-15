package jFx2.core.runtime

import jFx2.core.Component
import jFx2.core.Ctx
import jFx2.core.capabilities.*
import org.w3c.dom.Node

private class RootComponent(override val node: Node) : Component<Node>()

class ComponentMount(
    val ui: UiScope,
    val scope: NodeScope,
    private val disposeScope: DisposeScope
) {
    fun dispose() = disposeScope.dispose()
}

fun component(
    root: Node,
    owner: Component<*>? = null,
    ui: UiScope = UiScope(),
    ctx: Ctx = Ctx(),
    block: context(NodeScope) () -> Unit
): ComponentMount {
    val rootOwner = owner ?: RootComponent(root)

    val dispose = DisposeScope()
    val scope = NodeScope(
        ui = ui,
        parent = root,
        owner = rootOwner,
        ctx = ctx,
        dispose = dispose
    )

    block(scope)
    ui.build.flush()

    return ComponentMount(ui, scope, dispose)
}
