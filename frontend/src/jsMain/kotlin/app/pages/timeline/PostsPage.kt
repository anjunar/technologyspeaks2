package app.pages.timeline

import app.domain.core.Data
import app.domain.core.Table
import app.domain.time.Post
import app.services.ApplicationService
import jFx2.client.JsonClient
import jFx2.controls.button
import jFx2.controls.text
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
import jFx2.forms.input
import jFx2.router.PageInfo
import jFx2.router.navigate
import jFx2.state.ListProperty
import jFx2.virtual.RangeDataProvider
import jFx2.virtual.virtualList
import org.w3c.dom.HTMLDivElement
import kotlin.math.min

object PostsPage {

    class PostRangeProvider(
        private val maxItems: Int = 5000,
        private val pageSize: Int = 50
    ) : RangeDataProvider<Data<Post>?> {
        private val items = ListProperty<Data<Post>>()
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

            val target = min(toInclusive, maxItems - 1)

            while (items.size <= target && !reachedEnd) {
                val remaining = target - items.size + 1
                val limit = min(pageSize, remaining)
                if (limit <= 0) return

                val table = JsonClient.invoke<Table<Post>>("/service/timeline/posts?index=${items.size}&limit=$limit&sort=created:desc")

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

        override fun getOrNull(index: Int): Data<Post>? = items.getOrNull(index)

        override fun observeChanges(listener: (jFx2.state.ListChange<*>) -> Unit): jFx2.state.Disposable =
            items.observeChanges { listener(it) }

        fun upsert(post: Data<Post>) {
            val id = post.data.id?.get()
            if (id == null) {
                items.add(0, post)
                return
            }

            val index = items.indexOfFirst { it.data.id?.get() == id }
            if (index >= 0) items[index] = post else items.add(0, post)
        }

    }

    class Page(override val node: HTMLDivElement) : Component<HTMLDivElement>(), PageInfo {
        override val name: String = "Posts"
        override val width: Int = -1
        override val height: Int = -1
        override val resizable: Boolean = true
        override var close: () -> Unit = {}

        context(scope: NodeScope)
        fun afterBuild() {
            val provider = PostRangeProvider()
            scope.dispose.register(
                ApplicationService.messageBus.subscribe { message ->
                    when (message) {
                        is ApplicationService.Message.PostCreated -> provider.upsert(message.post)
                        is ApplicationService.Message.PostUpdated -> provider.upsert(message.post)
                    }
                }
            )

            val formular = Post()

            template {

                form(model = formular, clazz = Post::class) {

                    input("post") {

                        style {
                            margin = "12px"
                            padding = "12px"
                            width = "calc(100% - 48px)"
                            backgroundColor = "var(--color-background-secondary)"
                            fontSize = "24px"
                            borderRadius = "8px"
                        }

                        placeholder = "Nach was ist dir heute?"

                        onClick {
                            navigate("/timeline/posts/post")
                        }

                    }

                }

                virtualList(
                    dataProvider = provider,
                    estimateHeightPx = 44,
                    overscanPx = 240,
                    prefetchItems = 80,
                    renderer = { item, index ->

                        template {

                            form(model = item?.data, clazz = Post::class) {

                                className { "glass-border" }

                                if (item == null) {
                                    text("Loading...")
                                } else {
                                    postHeader {
                                        model(item)
                                    }

                                    editorView("editor") {
                                        style {
                                            height = "100%"
                                            width = "100%"
                                        }

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
                    }
                )
            }
        }

    }

    context(scope: NodeScope)
    fun page(block: context(NodeScope) Page.() -> Unit = {}): Page {
        val el = scope.create<HTMLDivElement>("div")
        el.classList.add("users-page")
        val c = Page(el)
        scope.attach(c)

        val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

        block(childScope, c)

        with(childScope) {
            scope.ui.build.afterBuild { c.afterBuild() }
        }

        return c
    }


}
