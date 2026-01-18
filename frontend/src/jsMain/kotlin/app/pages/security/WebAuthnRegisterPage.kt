package app.pages.security

import app.domain.security.WebAuthnRegister
import app.services.WebAuthnRegistrationClient
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

class RegisterPage(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    context(scope: NodeScope)
    fun afterBuild() {

        val registerForm = WebAuthnRegister()

        form {

            onSubmit {

                val email = registerForm.email.get()
                val nickname = registerForm.nickname.get()

                MainScope().launch {
                    try {
                        val resp = WebAuthnRegistrationClient.register(email, nickname)
                        console.log("Registration OK:", resp)
                    } catch (t: Throwable) {
                        console.error("WebAuthn registration failed", t)
                    }
                }

            }

            inputContainer("Email") {

                input("email", "email") {
                    validatorsProperty.add(EmailValidator())
                    subscribeBidirectional(registerForm.email, valueProperty)
                }

            }

            inputContainer("Nickname") {

                input("nickname") {
                    validatorsProperty.add(SizeValidator(3, 20))
                    subscribeBidirectional(registerForm.nickname, valueProperty)
                }

            }

            div {
                className { "button-container glass-border" }

                button("Register") { }
            }

        }

    }

}

context(scope: NodeScope)
fun registerPage(block: context(NodeScope) RegisterPage.() -> Unit = {}): RegisterPage {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("logout-page")
    val c = RegisterPage(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

    scope.ui.build.afterBuild {
        with(childScope) {
            c.afterBuild()
        }
    }

    block(childScope, c)

    return c
}
