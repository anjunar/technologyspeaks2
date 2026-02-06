package app.pages.timeline

import app.domain.core.Data
import app.domain.time.Post
import app.pages.timeline.PostsPage.Page
import app.services.ApplicationService
import jFx2.client.JsonClient
import jFx2.controls.button
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.style
import jFx2.core.dsl.subscribeBidirectional
import jFx2.core.template
import jFx2.forms.editor
import jFx2.forms.editor.plugins.basePlugin
import jFx2.forms.editor.plugins.headingPlugin
import jFx2.forms.editor.plugins.imagePlugin
import jFx2.forms.editor.plugins.linkPlugin
import jFx2.forms.editor.plugins.listPlugin
import jFx2.forms.form
import jFx2.layout.div
import jFx2.layout.hbox
import jFx2.layout.vbox
import jFx2.router.PageInfo
import jFx2.state.Property
import org.w3c.dom.HTMLDivElement

class PostPage(override val node: HTMLDivElement) : Component<HTMLDivElement>(), PageInfo {

    override val name: String = "Posts"
    override val width: Int = - 1
    override val height: Int = -1
    override val resizable: Boolean = true
    override var close: () -> Unit = {}

    private val model = Property(Data(Post(user = Property(ApplicationService.app.get().user))))

    fun model(value : Data<Post>) {
        model.set(value)
    }

    context(scope: NodeScope)
    fun afterBuild() {

        template {

            form {

                onSubmit {
                    if (model.get().data.id == null) {
                        val saved = JsonClient.post<Post, Data<Post>>("/service/timeline/posts/post", model.get().data)
                        ApplicationService.messageBus.publish(ApplicationService.Message.PostCreated(saved))
                        close()
                    } else {
                        val saved = JsonClient.put<Post, Data<Post>>("/service/timeline/posts/post", model.get().data)
                        ApplicationService.messageBus.publish(ApplicationService.Message.PostUpdated(saved))
                        close()
                    }
                }

                style {
                    padding = "10px"
                    height = "calc(100% - 20px)"
                }

                vbox {
                    postHeader {
                        model(model.get())
                    }

                    editor("editor") {
                        style {
                            flex = "1"
                        }

                        basePlugin { }
                        headingPlugin { }
                        listPlugin { }
                        linkPlugin { }
                        imagePlugin { }

                        subscribeBidirectional(model.get().data.editor, valueProperty)
                    }

                    button("Senden") {
                        style {
                            width = "100%"
                        }
                        className { "btn-secondary" }
                    }
                }

            }

        }

    }
}

context(scope: NodeScope)
fun postPage(block: context(NodeScope) PostPage.() -> Unit = {}): PostPage {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("post-page")
    el.classList.add("container")
    val c = PostPage(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

    block(childScope, c)

    with(childScope) {
        scope.ui.build.afterBuild { c.afterBuild() }
    }

    return c
}
