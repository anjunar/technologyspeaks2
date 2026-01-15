package jFx2.layout

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import org.w3c.dom.HTMLHRElement

class Hr(override val node: HTMLHRElement) : Component<HTMLHRElement>()

context(scope: NodeScope)
fun hr(block: context(NodeScope) Hr.() -> Unit = {}): Hr {
    val el = scope.create<HTMLHRElement>("hr")
    val c = Hr(el)
    scope.ui.dom.attach(scope.parent, c.node)

    val childScope = NodeScope(ui = scope.ui, parent = c.node, owner = c, ctx = scope.ctx, scope.dispose)
    block(childScope, c)
    return c
}

