package jFx2.controls

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import org.w3c.dom.HTMLSpanElement

class Span(override val node: HTMLSpanElement, val ui: UiScope) : Component<HTMLSpanElement>()

context(scope: NodeScope)
fun span(block: context(NodeScope) Span.() -> Unit = {}): Span {
    val el = scope.create<HTMLSpanElement>("span")
    val c = Span(el, scope.ui)
    scope.attach(c)

    val childScope = NodeScope(ui = scope.ui, parent = c.node, owner = c, ctx = scope.ctx, scope.dispose)
    block(childScope, c)

    return c
}
