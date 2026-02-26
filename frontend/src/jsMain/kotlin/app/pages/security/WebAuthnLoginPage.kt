package app.pages.security

import app.domain.security.WebAuthnLogin
import app.services.ApplicationService
import app.services.WebAuthnLoginClient
import jFx2.controls.button
import jFx2.controls.heading
import jFx2.controls.image
import jFx2.controls.text
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.onClick
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

class WebAuthnLoginPage(override val node: HTMLDivElement) : Component<HTMLDivElement>(), PageInfo {

    override val name: String = "Login mit WebAuthn"
    override val width: Int = -1
    override val height: Int = -1
    override val resizable: Boolean = false
    override var close: () -> Unit = {}

    context(scope: NodeScope)
    fun afterBuild() {

        val loginForm = WebAuthnLogin()

        template {
            form(model = loginForm, clazz = WebAuthnLogin::class) {

                onSubmit {

                    try {
                        val finishResponseText = WebAuthnLoginClient.login(this@form.model.email.get())

                        ApplicationService.invoke()

                        close()
                    } catch (t: Throwable) {
                        console.error("WebAuthn login failed", t)
                    }

                }

                image {
                    style {
                        width = "500px"
                    }

                    src = "/app/security/login_webauthn.png"
                }

                hbox {

                    style {
                        justifyContent = "center"
                    }

                    heading(3) {
                        text { "Möchtest du dich anmelden mit WebAuthn?" }
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

                }

                div {
                    className { "button-container" }

                    button("Abbrechen") {
                        onClick {
                            close()
                        }
                        className { "btn-secondary" }
                    }

                    button("Anmelden") {
                        className { "btn-danger" }
                    }
                }

            }
        }



    }

}

context(scope: NodeScope)
fun webAuthnLoginPage(block: context(NodeScope) WebAuthnLoginPage.() -> Unit = {}): WebAuthnLoginPage {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("login-page")
    val c = WebAuthnLoginPage(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

    with(childScope) {
        c.afterBuild()
    }

    block(childScope, c)

    return c
}
