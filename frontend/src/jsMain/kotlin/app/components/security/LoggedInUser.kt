package app.components.security

import app.services.ApplicationService
import jFx2.controls.image
import jFx2.controls.text
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.style
import jFx2.core.rendering.observeRender
import jFx2.core.template
import jFx2.layout.div
import jFx2.layout.hbox
import org.w3c.dom.HTMLDivElement

class LoggedInUser(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    context(scope: NodeScope)
    fun afterBuild() {

        val model = ApplicationService.app

        template {

            observeRender(model) { model ->

                hbox {

                    style {
                        alignItems = "center"
                        columnGap = "10px"
                    }

                    if (model.user.image.get() == null) {
                        div {
                            style {
                                fontSize = "32px"
                            }
                            className { "material-icons" }
                            text("account_circle")
                        }
                    } else {
                        image {
                            style {
                                height = "32px"
                                width = "32px"
                            }
                            src = model.user.image.get()?.thumbnailLink()!!
                        }
                    }

                    div {
                        if (model.user.info != null) {
                            text(model.user.info!!.firstName.get() + " " + model.user.info!!.lastName.get())
                        } else {
                            text(model.user.nickName.get())
                        }

                    }

                }


            }
        }

    }

}

context(scope: NodeScope)
fun loggedInUser(block: context(NodeScope) LoggedInUser.() -> Unit = {}): LoggedInUser {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("logged-in-user")
    val c = LoggedInUser(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

    block(childScope, c)

    with(childScope) {
        scope.ui.build.afterBuild { c.afterBuild() }
    }

    return c
}
