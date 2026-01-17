package jFx2.layout

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import org.w3c.dom.HTMLHRElement

class Hr(override val node: HTMLHRElement) : Component<HTMLHRElement>()

context(scope: NodeScope)
fun hr(block: context(NodeScope) Hr.() -> Unit = {}): Hr {
    val el = scope.create<HTMLHRElement>("hr")
    val c = Hr(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))
    block(childScope, c)
    return c
}
