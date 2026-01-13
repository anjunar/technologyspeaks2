package jFx2.controls

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import org.w3c.dom.HTMLDivElement

class Div(override val node: HTMLDivElement) : Component<HTMLDivElement>()


fun NodeScope.div(block: NodeScope.() -> Unit): Div {
    val el = create<HTMLDivElement>("div")
    val div = Div(el)

    attach(div)

    val childScope = NodeScope(ui, div.node, owner = div)
    childScope.block()

    return div
}