package app.pages.timeline

import app.components.commentable.commentsSection
import app.domain.core.Data
import app.domain.time.Post
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
import jFx2.forms.editorView
import jFx2.forms.form
import jFx2.layout.vbox
import jFx2.router.PageInfo
import jFx2.state.Property
import org.w3c.dom.HTMLDivElement

class PostViewPage(override val node: HTMLDivElement) : Component<HTMLDivElement>(), PageInfo {

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

            form(model = model.get().data, clazz = Post::class) {

                style {
                    padding = "10px"
                    height = "calc(100% - 20px)"
                }

                vbox {
                    postHeader {
                        model(this@PostViewPage.model.get())
                    }

                    editorView("editor") {
                        basePlugin { }
                        headingPlugin { }
                        listPlugin { }
                        linkPlugin { }
                        imagePlugin { }

                        subscribeBidirectional(this@form.model.editor, valueProperty)
                    }

                    commentsSection {
                        style {
                            flex = "1"
                            minHeight = "0px"
                        }
                        model(this@PostViewPage.model.get().links)
                    }

                }

            }

        }

    }
}

context(scope: NodeScope)
fun postViewPage(block: context(NodeScope) PostViewPage.() -> Unit = {}): PostViewPage {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("post-page")
    el.classList.add("container")
    val c = PostViewPage(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

    block(childScope, c)

    with(childScope) {
        scope.ui.build.afterBuild { c.afterBuild() }
    }

    return c
}
