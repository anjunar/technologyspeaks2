package jFx2.controls

import jFx2.core.Container
import jFx2.core.capabilities.NodeScope
import org.w3c.dom.HTMLSpanElement

class Span(override val node: HTMLSpanElement) : Container<HTMLSpanElement>(node)

fun NodeScope.span(block: NodeScope.() -> Unit): Span {
    val el = create<HTMLSpanElement>("span")
    val span = Span(el)

    attach(span)

    val childScope = NodeScope(ui, span.node, span, this.forms)
    childScope.block()

    return span
}