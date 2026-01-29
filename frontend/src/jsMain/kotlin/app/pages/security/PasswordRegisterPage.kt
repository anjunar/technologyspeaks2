package app.pages.security

import app.domain.security.PasswordRegister
import app.domain.security.WebAuthnRegister
import app.services.WebAuthnRegistrationClient
import jFx2.client.JsonClient
import jFx2.client.JsonResponse
import jFx2.controls.button
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.subscribeBidirectional
import jFx2.core.template
import jFx2.forms.EmailValidator
import jFx2.forms.SizeValidator
import jFx2.forms.form
import jFx2.forms.input
import jFx2.forms.inputContainer
import jFx2.layout.div
import jFx2.state.JobRegistry
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLDivElement
import org.w3c.fetch.RequestInit

class PasswordRegisterPage(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    context(scope: NodeScope)
    fun afterBuild() {

        val registerForm = PasswordRegister()

        template {
            form {

                onSubmit {

                    JobRegistry.instance.launch("Password Register") {
                        val post : JsonResponse = JsonClient.post("/service/security/register", registerForm)
                        println(post)
                    }

                }

                inputContainer("Nickname") {

                    input("nickname") {
                        validatorsProperty.add(SizeValidator(3, 20))
                        subscribeBidirectional(registerForm.nickName, valueProperty)
                    }

                }

                inputContainer("Email") {

                    input("email", "email") {
                        validatorsProperty.add(EmailValidator())
                        subscribeBidirectional(registerForm.email, valueProperty)
                    }

                }

                inputContainer("Password") {

                    input("password", "password") {
                        validatorsProperty.add(SizeValidator(5, 30))
                        subscribeBidirectional(registerForm.password, valueProperty)
                    }

                }

                div {
                    className { "button-container glass-border" }

                    button("Register") { }
                }

            }
        }



    }

}

context(scope: NodeScope)
fun passwordRegisterPage(block: context(NodeScope) PasswordRegisterPage.() -> Unit = {}): PasswordRegisterPage {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("logout-page")
    val c = PasswordRegisterPage(el)
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
