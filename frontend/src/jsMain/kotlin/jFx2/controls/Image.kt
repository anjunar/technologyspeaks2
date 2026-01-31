package jFx2.controls

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import org.w3c.dom.HTMLImageElement

class Image(override val node: HTMLImageElement) : Component<HTMLImageElement>() {

    var src : String
        get() = node.src
        set(value) { node.src = value }

}

context(scope: NodeScope)
fun image(block: context(NodeScope) Image.() -> Unit = {}): Image {
    val el = scope.create<HTMLImageElement>("img")
    val c = Image(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

    block(childScope, c)

    return c
}
