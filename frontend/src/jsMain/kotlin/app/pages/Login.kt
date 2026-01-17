package app.pages

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import org.w3c.dom.HTMLDivElement

class Login(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    init {
        node.textContent = "Login"
    }

}

context(scope: NodeScope)
fun loginPage(block: context(NodeScope) Login.() -> Unit = {}): Login {
    val el = scope.create<HTMLDivElement>("div")
    val c = Login(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx)

    block(childScope, c)

    return c
}
