package jFx2.controls

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import org.w3c.dom.HTMLDivElement

class Div(override val node: HTMLDivElement, val ui: UiScope) : Component<HTMLDivElement>()

context(scope: NodeScope)
fun div(block: context(NodeScope) Div.() -> Unit = {}): Div {
    val el = scope.create<HTMLDivElement>("div")
    val c = Div(el, scope.ui)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx)

    block(childScope, c)

    return c
}
