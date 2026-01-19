package app.pages.security

import app.domain.security.PasswordLogin
import app.domain.security.WebAuthnLogin
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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLDivElement

class PasswordLoginPage(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    context(scope: NodeScope)
    fun afterBuild() {

        val loginForm = PasswordLogin()

        form {

            onSubmit {

                MainScope().launch {
                    val post : JsonResponse = JsonClient.post("/service/security/login", loginForm)
                    println(post)
                }

            }

            inputContainer("Email") {

                input("email", "email") {
                    validatorsProperty.add(EmailValidator())
                    subscribeBidirectional(loginForm.email, valueProperty)
                }

            }

            inputContainer("Password") {

                input("password", "password") {
                    validatorsProperty.add(SizeValidator(5, 30))
                    subscribeBidirectional(loginForm.password, valueProperty)
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
fun passwordLoginPage(block: context(NodeScope) PasswordLoginPage.() -> Unit = {}): PasswordLoginPage {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("login-page")
    val c = PasswordLoginPage(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

    with(childScope) {
        c.afterBuild()
    }

    block(childScope, c)

    return c
}
