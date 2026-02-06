package app.pages.timeline

import app.domain.core.Data
import app.domain.core.Table
import app.domain.time.Post
import app.services.ApplicationService
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
import jFx2.virtual.AppendRangeDataProvider
import jFx2.virtual.RangeDataProvider
import jFx2.virtual.RangePage
import jFx2.virtual.virtualList
import org.w3c.dom.HTMLDivElement

object PostsPage {

    class PostRangeProvider(
        val maxItems: Int = 5000,
        val pageSize: Int = 50
    ) : RangeDataProvider<Data<Post>> by AppendRangeDataProvider(
        pageSize = pageSize,
        maxItems = maxItems,
        fetch = { index, limit ->
            val table = JsonClient.invoke<Table<Post>>(
                "/service/timeline/posts?index=$index&limit=$limit&sort=created:desc"
            )
            RangePage(rows = table.rows, totalCount = table.size)
        }
    )

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

            onDispose(
                ApplicationService.messageBus.subscribe { msg ->
                    when (msg) {
                        is ApplicationService.Message.PostCreated -> upsertPost(provider, msg.post, isCreated = true)
                        is ApplicationService.Message.PostUpdated -> upsertPost(provider, msg.post, isCreated = false)
                    }
                }
            )

            template {

                form {

                    onSubmit {

                        val response : Data<Post> = JsonClient.post("/service/timeline/posts/post", formular)
                        ApplicationService.messageBus.publish(ApplicationService.Message.PostCreated(response))

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
                        if (item == null) {
                            template {
                                div {
                                    className { "glass-border" }
                                    text("Loading...")
                                }
                            }
                            return@virtualList
                        }

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

    private fun upsertPost(
        provider: PostRangeProvider,
        post: Data<Post>,
        isCreated: Boolean
    ) {
        val id = post.data.id?.get() ?: return

        val items = provider.items
        val existingIndex = items.indexOfFirst { it.data.id?.get() == id }
        if (existingIndex >= 0) {
            items[existingIndex] = post
            return
        }

        if (!isCreated) return

        items.add(0, post)

        provider.totalCount.get()?.let { current ->
            val next = if (current < provider.maxItems) current + 1 else current
            provider.totalCount.set(next)
        }

        provider.totalCount.get()?.let { max ->
            while (items.size > max) items.removeAt(items.lastIndex)
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
