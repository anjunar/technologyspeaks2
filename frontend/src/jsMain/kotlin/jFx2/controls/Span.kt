package jFx2.controls

import jFx2.core.Container
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import org.w3c.dom.HTMLSpanElement

class Span(override val node: HTMLSpanElement, override val ui : UiScope) : Container<HTMLSpanElement>(node)

fun NodeScope.span(block: Span.() -> Unit): Span {
    val el = create<HTMLSpanElement>("span")
    val span = Span(el, ui)

    attach(span)

    span.bindChildren()

    span.block()

    return span
}