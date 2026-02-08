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
import jFx2.forms.input
import jFx2.layout.div
import jFx2.layout.hbox
import jFx2.layout.vbox
import jFx2.state.JobRegistry
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
        private val maxItems: Int = 5000,
        private val pageSize: Int = 50
    ) : RangeDataProvider<Data<Comment>?> {
        private val items = ListProperty<Data<Comment>>()
        private var reachedEnd: Boolean = false

        override val hasKnownCount: Boolean = false
        override val knownCount: Int = 0

        override val endReached: Boolean
            get() = reachedEnd || items.size >= maxItems

        override val loadedCount: Int
            get() = items.size

        override suspend fun ensureRange(from: Int, toInclusive: Int) {
            if (endReached) return
            if (toInclusive < 0) return
            if (toInclusive < items.size) return

            val target = kotlin.math.min(toInclusive, maxItems - 1)

            while (items.size <= target && !reachedEnd) {
                val remaining = target - items.size + 1
                val limit = kotlin.math.min(pageSize, remaining)
                if (limit <= 0) return

                val table = JsonClient.invoke<Table<Comment>>(
                    "${listUrl}?index=${items.size}&limit=$limit&sort=created:asc"
                )

                if (table.rows.isEmpty()) {
                    reachedEnd = true
                    return
                }

                items += table.rows

                if (table.rows.size < limit) {
                    reachedEnd = true
                    return
                }
            }
        }

        override fun getOrNull(index: Int): Data<Comment>? = items.getOrNull(index)

        override fun observeChanges(listener: (jFx2.state.ListChange<*>) -> Unit): jFx2.state.Disposable =
            items.observeChanges { listener(it) }

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
        val newText = Property("")

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

                    input("comment") {
                        style {
                            flex = "1"
                            padding = "8px"
                            borderRadius = "6px"
                            backgroundColor = "var(--color-background-secondary)"
                            border = "1px solid var(--color-background-primary)"
                        }
                        placeholder = "Kommentar schreiben..."

                        subscribeBidirectional(newText, valueProperty)
                    }

                    button("send") {
                        type("button")
                        className { "material-icons container hover" }

                        onClick {
                            val text = newText.get().trim()
                            if (text.isBlank()) return@onClick
                            if (busy.get()) return@onClick

                            JobRegistry.instance.launch("Comment", owner = this@CommentsSection) {
                                busy.set(true)
                                try {
                                    val created = JsonClient.post<CommentCreate, Data<Comment>>(
                                        serviceUrl(createLink.url),
                                        CommentCreate(text)
                                    )
                                    provider.upsert(created)
                                    newText.set("")
                                } finally {
                                    busy.set(false)
                                }
                            }
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
