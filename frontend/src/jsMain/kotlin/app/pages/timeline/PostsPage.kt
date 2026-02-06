package app.pages.timeline

import app.domain.core.Data
import app.domain.core.Table
import app.domain.time.Post
import jFx2.client.JsonClient
import jFx2.controls.button
import jFx2.controls.heading
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
import jFx2.virtual.RangeDataProvider
import jFx2.virtual.virtualList
import org.w3c.dom.HTMLDivElement
import kotlin.math.min

object PostsPage {

    class PostRangeProvider(
        private val maxItems: Int = 5000,
        private val pageSize: Int = 50
    ) : RangeDataProvider<Data<Post>> {
        private val items = ArrayList<Data<Post>>()
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

        override fun getOrNull(index: Int): Data<Post> = items[index]

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

            val formular = Post()

            template {

                form {

                    onSubmit {

                        val response : Data<Post> = JsonClient.post("/service/timeline/posts/post", formular)

                    }

                    editor("editor") {
                        style {
                            height = "300px"
                            width = "100%"
                        }

                        basePlugin { }
                        headingPlugin { }
                        listPlugin { }
                        linkPlugin { }
                        imagePlugin { }

                        subscribeBidirectional(formular.editor, valueProperty)
                    }

                    button("Posten") {

                    }

                }

                virtualList(
                    dataProvider = provider,
                    estimateHeightPx = 44,
                    overscanPx = 240,
                    prefetchItems = 80,
                    renderer = { item, index ->

                        template {

                            form {

                                className { "glass-border" }

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

                                    subscribeBidirectional(item.data.editor, valueProperty)

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
