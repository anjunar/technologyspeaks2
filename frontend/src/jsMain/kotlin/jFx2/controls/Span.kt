package jFx2.controls

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import org.w3c.dom.HTMLDivElement

class Span(override val node: HTMLDivElement) : Component<HTMLDivElement>()


fun NodeScope.span(block: NodeScope.() -> Unit): Span {
    val el = create<HTMLDivElement>("span")
    val span = Span(el)

    attach(span)

    val childScope = NodeScope(ui, span.node, span, this.forms)
    childScope.block()

    return span
}