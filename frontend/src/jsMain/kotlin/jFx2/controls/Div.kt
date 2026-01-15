package jFx2.controls

import jFx2.core.Container
import jFx2.core.capabilities.NodeScope
import org.w3c.dom.HTMLDivElement

class Div(override val node: HTMLDivElement) : Container<HTMLDivElement>(node)

fun NodeScope.div(block: NodeScope.() -> Unit): Div {
    val el = create<HTMLDivElement>("div")
    val div = Div(el)

    ui.attach(parent, div)

    div.bindChildren()

    val childScope = NodeScope(
        ui = ui,
        parent = div.node,
        owner = div,
        forms = this.forms
    )
    childScope.block()

    return div
}
