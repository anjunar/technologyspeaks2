package jFx2.forms

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import org.w3c.dom.HTMLOptionElement

class Option(override val node: HTMLOptionElement) : Component<HTMLOptionElement>()

context(scope: NodeScope)
fun option(value : String, block: context(NodeScope) Option.() -> Unit = {}): Option {
    val el = scope.create<HTMLOptionElement>("option")
    el.value = value
    val c = Option(el)
    scope.attach(c)

    val childScope = scope.fork(
        parent = c.node,
        owner = c,
        ctx = scope.ctx,
        insertPoint = ElementInsertPoint(c.node)
    )

    block(childScope, c)

    return c
}
