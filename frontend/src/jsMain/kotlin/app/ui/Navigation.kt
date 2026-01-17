package app.ui

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import org.w3c.dom.HTMLDivElement

class Navigation(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    context(scope: NodeScope)
    fun afterBuild() {



    }

}

context(scope: NodeScope)
fun div(block: context(NodeScope) Navigation.() -> Unit = {}): Navigation {
    val el = scope.create<HTMLDivElement>("div")
    val c = Navigation(el)
    scope.attach(c)

    val childScope = scope.fork(
        parent = c.node,
        owner = c,
        ctx = scope.ctx,
        insertPoint = ElementInsertPoint(c.node)
    )

    scope.ui.build.afterBuild {
        with(childScope) {
            c.afterBuild()
        }
    }

    block(childScope, c)

    return c
}
