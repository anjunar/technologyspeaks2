package app.pages.security

import app.domain.security.PasswordRegister
import jFx2.client.JsonClient
import jFx2.client.JsonResponse
import jFx2.controls.button
import jFx2.controls.heading
import jFx2.controls.image
import jFx2.controls.text
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.style
import jFx2.core.dsl.subscribeBidirectional
import jFx2.core.template
import jFx2.forms.EmailValidator
import jFx2.forms.SizeValidator
import jFx2.forms.form
import jFx2.forms.input
import jFx2.forms.inputContainer
import jFx2.layout.div
import jFx2.layout.hbox
import jFx2.router.PageInfo
import jFx2.state.JobRegistry
import kotlinx.browser.window
import org.w3c.dom.HTMLDivElement

class PasswordRegisterPage(override val node: HTMLDivElement) : Component<HTMLDivElement>(), PageInfo {

    override val name: String = "Register"
    override val width: Int = -1
    override val height: Int = -1
    override val resizable: Boolean = false
    override var close: () -> Unit = {}

    context(scope: NodeScope)
    fun afterBuild() {

        val registerForm = PasswordRegister()

        template {
            form(model = registerForm, clazz = PasswordRegister::class) {

                onSubmit {

                    val post : JsonResponse = JsonClient.post("/service/security/register", this@form.model)
                    close()

                }

                image {
                    style {
                        width = "500px"
                    }

                    src = "/app/security/register.png"
                }

                hbox {

                    style {
                        justifyContent = "center"
                    }

                    heading(3) {
                        text { "Möchtest du dich Registrieren?" }
                    }
                }

                div {

                    style {
                        padding = "20px"
                    }

                    inputContainer("Email") {

                        input("email", "email") {
                            validatorsProperty.add(EmailValidator())
                            subscribeBidirectional(this@form.model.email, valueProperty)
                        }

                    }

                    inputContainer("Password") {

                        input("password", "password") {
                            validatorsProperty.add(SizeValidator(5, 30))
                            subscribeBidirectional(this@form.model.password, valueProperty)
                        }

                    }
                }

                div {
                    className { "button-container" }

                    button("Abbrechen") {
                        onClick {
                            close()
                        }
                        className { "btn-secondary" }
                    }

                    button("Registrieren") {
                        className { "btn-danger" }
                    }
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
