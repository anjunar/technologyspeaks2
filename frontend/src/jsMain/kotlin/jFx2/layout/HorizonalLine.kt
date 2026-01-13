package jFx2.layout

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import org.w3c.dom.HTMLDivElement

class HorizontalLine(override val node: HTMLDivElement) : Component<HTMLDivElement>()

fun NodeScope.hr(block: NodeScope.() -> Unit): HorizontalLine {
    val el = create<HTMLDivElement>("hr")
    val horizontalLine = HorizontalLine(el)

    attach(horizontalLine)

    val childScope = NodeScope(ui, horizontalLine.node, horizontalLine)
    childScope.block()

    return horizontalLine
}