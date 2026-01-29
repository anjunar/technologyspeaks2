package jFx2.layout

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.renderFields
import org.w3c.dom.HTMLDivElement

class Div(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    context(scope: NodeScope)
    fun afterBuild() {
        renderFields(*this@Div.children.toTypedArray())
    }


}

context(scope: NodeScope)
fun div(block: context(NodeScope) Div.() -> Unit = {}): Div {
    val el = scope.create<HTMLDivElement>("div")
    val c = Div(el)
    scope.attach(c)

    val childScope = scope.fork(
        parent = c.node,
        owner = c,
        ctx = scope.ctx,
        insertPoint = ElementInsertPoint(c.node)
    )


    block(childScope, c)

    with(childScope) {
        scope.ui.build.afterBuild { c.afterBuild() }
    }

    return c
}

context(scope: NodeScope)
fun hbox(block: context(NodeScope) Div.() -> Unit = {}): Div {
    val el = scope.create<HTMLDivElement>("div")
    val c = Div(el)
    scope.attach(c)

    val childScope = scope.fork(
        parent = c.node,
        owner = c,
        ctx = scope.ctx,
        insertPoint = ElementInsertPoint(c.node)
    )


    block(childScope, c)

    with(childScope) {
        scope.ui.build.afterBuild { c.afterBuild() }
    }

    el.classList.add("hbox")

    return c
}

context(scope: NodeScope)
fun vbox(block: context(NodeScope) Div.() -> Unit = {}): Div {
    val el = scope.create<HTMLDivElement>("div")
    val c = Div(el)
    scope.attach(c)

    val childScope = scope.fork(
        parent = c.node,
        owner = c,
        ctx = scope.ctx,
        insertPoint = ElementInsertPoint(c.node)
    )

    block(childScope, c)

    with(childScope) {
        scope.ui.build.afterBuild { c.afterBuild() }
    }

    el.classList.add("vbox")

    return c
}