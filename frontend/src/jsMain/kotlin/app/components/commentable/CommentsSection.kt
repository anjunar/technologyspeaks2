package app.components.commentable

import app.domain.core.Data
import app.domain.core.Link
import app.domain.core.Table
import app.domain.shared.Comment
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
import jFx2.core.template
import jFx2.forms.editor
import jFx2.forms.editor.plugins.*
import jFx2.forms.editorView
import jFx2.forms.form
import jFx2.layout.div
import jFx2.layout.hbox
import jFx2.layout.vbox
import jFx2.state.ListProperty
import jFx2.state.Property
import jFx2.virtual.RangeDataProvider
import jFx2.virtual.virtualList
import org.w3c.dom.HTMLDivElement

private fun serviceUrl(url: String): String =
    if (url.startsWith("/service/")) url else "/service$url"

class CommentsSection(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    private var links: ListProperty<Link>? = null
    private var listRel: String = "comments"
    private var createRel: String = "comment"

    fun model(
        links: ListProperty<Link>,
        listRel: String = "comments",
        createRel: String = "comment"
    ) {
        this.links = links
        this.listRel = listRel
        this.createRel = createRel
    }

    private class CommentRangeProvider(
        private val listUrl: String,
        override val maxItems: Int = 5000,
        override val pageSize: Int = 50
    ) : RangeDataProvider<Data<Comment>>() {

        override suspend fun fetch(index: Int, limit: Int): Table<Data<Comment>> =
            JsonClient.invoke<Table<Data<Comment>>>(
                "${listUrl}?index=${items.size}&limit=$limit&sort=created:asc"
            )

        fun upsert(comment: Data<Comment>) {
            val id = comment.data.id?.get()
            if (id == null) {
                items.add(comment)
                return
            }

            val index = items.indexOfFirst { it.data.id?.get() == id }
            if (index >= 0) items[index] = comment else items.add(comment)
        }
    }

    context(scope: NodeScope)
    fun afterBuild() {
        val links = links ?: return

        val listLink = links.get().firstOrNull { it.rel == listRel } ?: return
        val createLink = links.get().firstOrNull { it.rel == createRel } ?: return

        val provider = CommentRangeProvider(serviceUrl(listLink.url))

        val busy = Property(false)
        val newText = Property(Comment())

        template {
            vbox {
                style {
                    setProperty("gap", "8px")
                    marginTop = "8px"
                }

                div {
                    style {
                        flex = "1"
                        minHeight = "0px"
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
                                                text(user.nickName.get())
                                            }

                                        }

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
                            }
                        }
                    )
                }

                form(model = newText.get(), clazz = Comment::class) {
                    style {
                        height = "120px"
                    }

                    onSubmit {
                        busy.set(true)
                        try {
                            val created =
                                JsonClient.post<Comment, Data<Comment>>(serviceUrl(createLink.url), this@form.model)
                            provider.upsert(created)
                            this@form.model.editor.set("")
                        } finally {
                            busy.set(false)
                        }
                    }

                    editor("editor") {
                        basePlugin { }
                        headingPlugin { }
                        listPlugin { }
                        linkPlugin { }
                        imagePlugin { }

                        subscribeBidirectional(this@form.model.editor, valueProperty)
                    }


                    button("send") {
                        className { "material-icons container hover" }
                    }
                }

            }
        }
    }
}

context(scope: NodeScope)
fun commentsSection(block: context(NodeScope) CommentsSection.() -> Unit = {}): CommentsSection {
    val el = scope.create<HTMLDivElement>("div")
    val c = CommentsSection(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))
    block(childScope, c)

    with(childScope) {
        scope.ui.build.afterBuild { c.afterBuild() }
    }

    return c
}
