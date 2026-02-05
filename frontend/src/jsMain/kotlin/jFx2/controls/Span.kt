package jFx2.controls

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.renderFields
import org.w3c.dom.HTMLSpanElement

class Span(override val node: HTMLSpanElement, val ui: UiScope) : Component<HTMLSpanElement>() {

    context(scope: NodeScope)
    fun afterBuild() {
        renderFields(*this@Span.children.toTypedArray())
    }

}

context(scope: NodeScope)
fun span(block: context(NodeScope) Span.() -> Unit = {}): Span {
    val el = scope.create<HTMLSpanElement>("span")
    val c = Span(el, scope.ui)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))
    block(childScope, c)

    scope.ui.build.afterBuild { with(childScope) { c.afterBuild() } }

    return c
}
