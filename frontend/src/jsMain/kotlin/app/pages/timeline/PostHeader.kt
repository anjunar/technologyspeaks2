package app.pages.timeline

import app.domain.core.Data
import app.domain.core.User
import app.domain.time.Post
import app.pages.timeline.PostsPage.Page
import jFx2.controls.button
import jFx2.controls.heading
import jFx2.controls.image
import jFx2.controls.text
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.style
import jFx2.core.template
import jFx2.layout.div
import jFx2.layout.hbox
import jFx2.layout.vbox
import jFx2.router.navigateByRel
import jFx2.state.Property
import org.w3c.dom.HTMLDivElement

class PostHeader(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    private val model = Property(Data(Post()))

    fun model(value : Data<Post>) {
        model.set(value)
    }

    context(scope: NodeScope)
    fun afterBuild() {
        template {
            hbox {

                style {
                    columnGap = "10px"
                    alignItems = "center"
                }

                image {
                    style {
                        height = "48px"
                        width = "48px"
                    }
                    src = model.get().data.user!!.get().image.get()?.thumbnailLink()!!
                }

                vbox {
                    heading(3) {
                        text(model.get().data.user!!.get().nickName.get())
                    }
                }

                div {
                    style {
                        flex = "1"
                    }
                }

                navigateByRel("read", model.get().links) { navigate ->
                    button("edit") {
                        type("button")
                        className { "material-icons" }
                        onClick {
                            navigate()
                        }
                    }
                }
            }
        }
    }

}

context(scope: NodeScope)
fun postHeader(block: context(NodeScope) PostHeader.() -> Unit = {}): PostHeader {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("users-page")
    val c = PostHeader(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

    block(childScope, c)

    with(childScope) {
        scope.ui.build.afterBuild { c.afterBuild() }
    }

    return c
}
