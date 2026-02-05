package app.pages.security

import app.services.ApplicationService
import jFx2.controls.button
import jFx2.controls.heading
import jFx2.controls.image
import jFx2.controls.span
import jFx2.controls.text
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.style
import jFx2.core.template
import jFx2.forms.form
import jFx2.layout.div
import jFx2.layout.hbox
import jFx2.router.PageInfo
import jFx2.state.JobRegistry
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.HTMLDivElement
import org.w3c.fetch.RequestInit

class LogoutPage(override val node: HTMLDivElement) : Component<HTMLDivElement>(), PageInfo {

    override val name: String = "Abmelden"
    override val width: Int = -1
    override val height: Int = -1
    override val resizable: Boolean = false
    override var close: () -> Unit = {}

    context(scope: NodeScope)
    fun afterBuild() {

        template {
            form {

                onSubmit {

                    JobRegistry.instance.launch("Logout"){
                        window.fetch("/service/security/logout", RequestInit("POST")).await()

                        ApplicationService.invoke()

                        close()
                    }

                }

                image {
                    style {
                        width = "500px"
                    }
                    src = "/app/security/logout.png"
                }

                hbox {

                    style {
                        justifyContent = "center"
                    }

                    heading(3) {
                        text { "Möchtest du dich wirklich abmelden?" }
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

                    button("Abmelden") {
                        className { "btn-danger" }
                    }
                }


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
