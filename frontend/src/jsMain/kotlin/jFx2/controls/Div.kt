package jFx2.controls

import jFx2.core.Container
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import org.w3c.dom.HTMLDivElement

class Div(override val node: HTMLDivElement, override val ui : UiScope) : Container<HTMLDivElement>(node)

fun NodeScope.div(block: Div.() -> Unit): Div {
    val el = create<HTMLDivElement>("div")
    val div = Div(el, ui)

    ui.attach(parent, div)

    div.bindChildren()

    div.block()

    return div
}
