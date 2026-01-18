package app.pages.security

import app.domain.security.WebAuthnLogin
import app.services.WebAuthnLoginClient
import jFx2.controls.button
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.subscribeBidirectional
import jFx2.forms.EmailValidator
import jFx2.forms.form
import jFx2.forms.input
import jFx2.forms.inputContainer
import jFx2.layout.div
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLDivElement

class LoginPage(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    context(scope: NodeScope)
    fun afterBuild() {

        val loginForm = WebAuthnLogin()

        form {

            onSubmit {

                MainScope().launch {
                    try {
                        val finishResponseText = WebAuthnLoginClient.login(loginForm.email.get())
                        console.log("Login OK:", finishResponseText)
                    } catch (t: Throwable) {
                        console.error("WebAuthn login failed", t)
                    }
                }

            }

            inputContainer("Email") {

                input("email", "email") {
                    validatorsProperty.add(EmailValidator())
                    subscribeBidirectional(loginForm.email, valueProperty)
                }

            }

            div {
                className { "button-container glass-border" }

                button("Login") { }
            }

        }

    }

}

context(scope: NodeScope)
fun loginPage(block: context(NodeScope) LoginPage.() -> Unit = {}): LoginPage {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("login-page")
    val c = LoginPage(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

    with(childScope) {
        c.afterBuild()
    }

    block(childScope, c)

    return c
}
