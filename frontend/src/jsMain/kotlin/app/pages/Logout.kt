package app.pages

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import org.w3c.dom.HTMLDivElement

class Logout(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    init {
        node.textContent = "Logout"
    }

}

context(scope: NodeScope)
fun logoutPage(block: context(NodeScope) Logout.() -> Unit = {}): Logout {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("logout-page")
    val c = Logout(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx)

    block(childScope, c)

    return c
}
