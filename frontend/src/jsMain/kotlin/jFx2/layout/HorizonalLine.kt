package jFx2.layout

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import org.w3c.dom.HTMLDivElement

class HorizontalLine(override val node: HTMLDivElement, override val ui : UiScope) : Component<HTMLDivElement>()

fun NodeScope.hr(block: NodeScope.() -> Unit): HorizontalLine {
    val el = create<HTMLDivElement>("hr")
    val horizontalLine = HorizontalLine(el, ui)

    attach(horizontalLine)

    val childScope = NodeScope(ui, horizontalLine.node, horizontalLine, this.forms)
    childScope.block()

    return horizontalLine
}