package app.pages.security

import app.domain.security.PasswordLogin
import app.domain.security.WebAuthnLogin
import app.services.ApplicationService
import app.services.WebAuthnLoginClient
import jFx2.client.JsonClient
import jFx2.client.JsonResponse
import jFx2.controls.button
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.subscribeBidirectional
import jFx2.forms.EmailValidator
import jFx2.forms.SizeValidator
import jFx2.forms.form
import jFx2.forms.input
import jFx2.forms.inputContainer
import jFx2.layout.div
import jFx2.state.JobRegistry
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLDivElement
import org.w3c.fetch.RequestInit

class LogoutPage(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    context(scope: NodeScope)
    fun afterBuild() {

        form {

            onSubmit {

                JobRegistry.instance.launch("Logout"){
                    window.fetch("/service/security/logout", RequestInit("POST")).await()

                    ApplicationService.invoke()
                }

            }

            div {
                className { "button-container glass-border" }

                button("Lgout") { }
            }

        }

    }

}

context(scope: NodeScope)
fun logoutPage(block: context(NodeScope) LogoutPage.() -> Unit = {}): LogoutPage {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("login-page")
    val c = LogoutPage(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

    with(childScope) {
        c.afterBuild()
    }

    block(childScope, c)

    return c
}
