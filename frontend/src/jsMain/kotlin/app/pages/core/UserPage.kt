package app.pages.core

import app.pages.security.PasswordLoginPage
import jFx2.controls.text
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.layout.div
import org.w3c.dom.HTMLDivElement

class UserPage(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    context(scope: NodeScope)
    fun afterBuild() {

        div {
            text {
                "test"
            }
        }

    }

}

context(scope: NodeScope)
fun usersPage(block: context(NodeScope) UserPage.() -> Unit = {}): UserPage {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("login-page")
    val c = UserPage(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

    with(childScope) {
        c.afterBuild()
    }

    block(childScope, c)

    return c
}


