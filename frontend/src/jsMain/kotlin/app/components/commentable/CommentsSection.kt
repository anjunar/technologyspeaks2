package app.components.commentable

import app.domain.core.Data
import app.domain.core.Link
import app.domain.core.Table
import app.domain.shared.Comment
import app.domain.shared.CommentCreate
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
import jFx2.forms.form
import jFx2.forms.input
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

        override suspend fun fetch(index : Int, limit: Int): Table<Data<Comment>> = JsonClient.invoke<Table<Data<Comment>>>(
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

                hbox {

                    style {
                        columnGap = "8px"
                        alignItems = "center"
                    }

                    form(model = newText.get(), clazz = Comment::class) {

                        onSubmit {
                            val text = this@form.model.text.get().trim()

                            busy.set(true)
                            try {
                                val created = JsonClient.post<CommentCreate, Data<Comment>>(
                                    serviceUrl(createLink.url),
                                    CommentCreate(text)
                                )
                                provider.upsert(created)
                                this@form.model.text.set("")
                            } finally {
                                busy.set(false)
                            }
                        }

                        input("comment") {
                            style {
                                flex = "1"
                                padding = "8px"
                                borderRadius = "6px"
                                backgroundColor = "var(--color-background-secondary)"
                                border = "1px solid var(--color-background-primary)"
                            }
                            placeholder = "Kommentar schreiben..."

                            subscribeBidirectional(this@form.model.text, valueProperty)
                        }

                        button("send") {
                            type("button")
                            className { "material-icons container hover" }
                        }
                    }

                }

                div {
                    style {
                        height = "180px"
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
                                    hbox {
                                        style {
                                            columnGap = "8px"
                                            alignItems = "center"
                                            padding = "6px 0"
                                        }

                                        val user = item.data.user?.get()
                                        val img = user?.image?.get()?.thumbnailLink()
                                        if (img != null) {
                                            image {
                                                style {
                                                    height = "28px"
                                                    width = "28px"
                                                    borderRadius = "999px"
                                                }
                                                src = img
                                            }
                                        }

                                        vbox {
                                            style {
                                                setProperty("gap", "2px")
                                            }
                                            if (user != null) {
                                                text(user.nickName.get())
                                            }
                                            text(item.data.text.get())
                                        }
                                    }
                                }
                            }
                        }
                    )
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
