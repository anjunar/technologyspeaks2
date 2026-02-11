@file:Suppress("UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")

package app.pages.timeline

import app.components.commentable.commentsSection
import app.components.timeline.postHeader
import app.domain.core.AbstractEntity
import app.domain.core.Data
import app.domain.core.Table
import app.domain.shared.FirstComment
import app.domain.time.Post
import app.services.ApplicationService
import jFx2.client.JsonClient
import jFx2.controls.button
import jFx2.controls.image
import jFx2.controls.text
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.style
import jFx2.core.dsl.subscribeBidirectional
import jFx2.core.rendering.condition
import jFx2.core.template
import jFx2.forms.editor
import jFx2.forms.editor.plugins.basePlugin
import jFx2.forms.editor.plugins.headingPlugin
import jFx2.forms.editor.plugins.imagePlugin
import jFx2.forms.editor.plugins.linkPlugin
import jFx2.forms.editor.plugins.listPlugin
import jFx2.forms.editorView
import jFx2.forms.form
import jFx2.layout.div
import jFx2.layout.hbox
import jFx2.layout.vbox
import jFx2.router.PageInfo
import jFx2.state.Property
import jFx2.virtual.RangeDataProvider
import jFx2.virtual.virtualList
import org.w3c.dom.HTMLDivElement

private class RangeProvider(
    private val listUrl: String,
    override val maxItems: Int = 5000,
    override val pageSize: Int = 50
) : RangeDataProvider<Data<out AbstractEntity>>() {

    override suspend fun fetch(index: Int, limit: Int): Table<out Data<out AbstractEntity>> =
        JsonClient.invoke<Table<Data<FirstComment>>>(
            "${listUrl}?index=${index - 1}&limit=$limit&sort=created:asc"
        )

}

private fun serviceUrl(url: String): String =
    if (url.startsWith("/service/")) url else "/service$url"

class PostViewPage(override val node: HTMLDivElement) : Component<HTMLDivElement>(), PageInfo {

    override val name: String = "Posts"
    override val width: Int = - 1
    override val height: Int = -1
    override val resizable: Boolean = true
    override var close: () -> Unit = {}

    private var createRel: String = "save"

    private val model = Property(Data(Post(user = Property(ApplicationService.app.get().user))))

    fun model(value : Data<Post>) {
        model.set(value)
    }

    context(scope: NodeScope)
    fun afterBuild() {

        val createLink = model.get().links.get().firstOrNull { it.rel == createRel } ?: return
        val listLink = model.get().links.firstOrNull { it.rel == "comments" } ?: return
        val provider = RangeProvider(serviceUrl(listLink.url))
        provider.upsert(model.get())

        template {

            vbox {

                div {

                    style {
                        flex = "1"
                    }

                    virtualList(
                        dataProvider = provider,
                        estimateHeightPx = 44,
                        overscanPx = 120,
                        prefetchItems = 40,
                        renderer = { item, _ ->

                            template {
                                if (item == null) {
                                    text("Loading...")
                                } else {


                                    when(item.data) {
                                        is Post -> {
                                            form(model = item.data, clazz = Post::class) {

                                                style {
                                                    padding = "10px"
                                                    height = "calc(100% - 20px)"
                                                }

                                                vbox {
                                                    postHeader {
                                                        model(item as Data<Post>)
                                                    }

                                                    editorView("editor") {
                                                        basePlugin { }
                                                        headingPlugin { }
                                                        listPlugin { }
                                                        linkPlugin { }
                                                        imagePlugin { }

                                                        subscribeBidirectional(this@form.model.editor, valueProperty)
                                                    }
                                                }
                                            }
                                        }
                                        is FirstComment -> {
                                            vbox {

                                                className { "glass-border" }

                                                hbox {
                                                    style {
                                                        columnGap = "8px"
                                                        alignItems = "center"
                                                    }

                                                    val user = item.data.user!!.get()
                                                    val img = user.image.get()?.thumbnailLink()
                                                    if (img == null) {
                                                        div {
                                                            text("user")
                                                            className { "material-icons" }
                                                            style {
                                                                fontSize = "48px"
                                                            }
                                                        }
                                                    } else {
                                                        image {
                                                            style {
                                                                height = "48px"
                                                                width = "48px"
                                                            }
                                                            src = img
                                                        }
                                                    }

                                                    div {
                                                        style {
                                                            flex = "1"
                                                        }
                                                        text(user.nickName.get())
                                                    }

                                                    button("delete") {
                                                        type("button")
                                                        className { "material-icons" }
                                                        onClick {

                                                        }
                                                    }

                                                }

                                                condition(item.data.editable) {
                                                    then {
                                                        form(model = item.data, clazz = FirstComment::class) {
                                                            onSubmit {
                                                                val created = JsonClient.post<FirstComment, Data<FirstComment>>("/service" + createLink.url, this@form.model)
                                                                this@form.model.editable.set(false)
                                                            }

                                                            editor("editor") {

                                                                style {
                                                                    height = "300px"
                                                                }

                                                                basePlugin { }
                                                                headingPlugin { }
                                                                listPlugin { }
                                                                linkPlugin { }
                                                                imagePlugin { }

                                                                subscribeBidirectional(this@form.model.editor, valueProperty)
                                                            }

                                                            button("send") {}
                                                        }

                                                    }
                                                    elseDo {
                                                        editorView("editor") {
                                                            basePlugin { }
                                                            headingPlugin { }
                                                            listPlugin { }
                                                            linkPlugin { }
                                                            imagePlugin { }

                                                            subscribeBidirectional(item.data.editor, valueProperty)
                                                        }
                                                    }
                                                }

                                                commentsSection {
                                                    model(item as Data<FirstComment>)
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
                }

                button("Neuer Kommentar") {
                    type("button")
                    onClick {
                        val firstComment = FirstComment()
                        firstComment.editable.set(true)
                        firstComment.user = Property(ApplicationService.app.get().user)
                        provider.upsert(Data(firstComment))
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
