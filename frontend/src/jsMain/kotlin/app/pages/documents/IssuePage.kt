package app.pages.documents

import app.components.commentable.commentsSection
import app.components.likeable.likeButton
import app.components.timeline.postHeader
import app.domain.core.AbstractEntity
import app.domain.core.Data
import app.domain.core.Table
import app.domain.documents.Issue
import app.domain.documents.IssueCreated
import app.domain.documents.IssueUpdated
import app.domain.shared.FirstComment
import app.domain.timeline.Post
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
import jFx2.core.rendering.condition
import jFx2.core.template
import jFx2.forms.editor
import jFx2.forms.editor.plugins.basePlugin
import jFx2.forms.editor.plugins.headingPlugin
import jFx2.forms.editor.plugins.imagePlugin
import jFx2.forms.editor.plugins.linkPlugin
import jFx2.forms.editor.plugins.listPlugin
import jFx2.forms.form
import jFx2.forms.input
import jFx2.forms.inputContainer
import jFx2.layout.div
import jFx2.layout.vbox
import jFx2.router.PageInfo
import jFx2.state.JobRegistry
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

class IssuePage(override val node: HTMLDivElement) : Component<HTMLDivElement>(), PageInfo {
    override val name: String = "Aufgabe"
    override val width: Int = -1
    override val height: Int = -1
    override val resizable: Boolean = true
    override var close: () -> Unit = {}

    private val model = Property(Issue())
    private val documentId = Property("")

    fun model(value : Issue) {
        model.set(value)
    }

    fun documentId(value : String) {
        documentId.set(value)
    }

    context(scope: NodeScope)
    fun afterBuild() {

        val createLink = model.get().links.get().firstOrNull { it.rel == "save" } ?: return
        val listLink = model.get().links.firstOrNull { it.rel == "comments" } ?: return
        val provider = RangeProvider("/service" + listLink.url)
        provider.upsert(Data(model.get()))

        template {

            style {
                height = "100%"
            }

            form(model = model.get(), clazz = Issue::class) {

                style {
                    padding = "10px"
                    height = "calc(100% - 20px)"
                }

                onSubmit {
                    val link = model.links.get().find { it.rel == "update" || it.rel == "save" }

                    val entity = JsonClient.invoke<Issue, Data<Issue>>(link!!, model)

                    when(link.rel) {
                        "update" -> ApplicationService.messageBus.publish(IssueUpdated(entity))
                        "save" -> ApplicationService.messageBus.publish(IssueCreated(entity))
                    }

                    close()
                }

                vbox {

                    div {

                        style {
                            flex = "1"
                            minHeight = "0px"
                        }

                        virtualList(
                            dataProvider = provider,
                            estimateHeightPx = 240,
                            overscanPx = 240,
                            prefetchItems = 40,
                            renderer = { item, _ ->

                                template {
                                    if (item == null) {
                                        div {
                                            className { "glass-border" }

                                            style {
                                                height = "200px"
                                            }

                                            postHeader {

                                            }

                                            vbox {
                                                style {
                                                    justifyContent = "center"
                                                    alignItems = "center"
                                                }
                                                text("Laden...")
                                            }
                                        }
                                    } else {


                                        when(item.data) {
                                            is Issue -> {
                                                form(model = item.data, clazz = Issue::class) {

                                                    style {
                                                        padding = "10px"
                                                        height = "calc(100% - 20px)"
                                                    }

                                                    vbox {
                                                        inputContainer("Titel") {
                                                            input("title") {
                                                                subscribeBidirectional(model.title, valueProperty)
                                                            }
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

                                                            button("save") {
                                                                className { "material-icons" }
                                                            }

                                                            subscribeBidirectional(model.editor, valueProperty)
                                                        }
                                                    }
                                                }
                                            }
                                            is FirstComment -> {
                                                vbox {

                                                    className { "glass-border" }

                                                    postHeader {
                                                        model(item.data)

                                                        onDelete {
                                                            JobRegistry.instance.launch("Comment Remove", "Comment") {
                                                                val deleteLink = item.data.links.find { it.rel == "delete" }
                                                                JsonClient.delete("/service" + deleteLink!!.url, item.data)
                                                                provider.remove(item)
                                                            }
                                                        }

                                                        onUpdate {
                                                            val editable = item.data.editable
                                                            editable.set(! editable.get())
                                                        }
                                                    }

                                                    form(model = item.data, clazz = FirstComment::class) {
                                                        onSubmit {
                                                            JsonClient.post<FirstComment, Data<FirstComment>>("/service" + createLink.url, model)
                                                            item.data.editable.set(false)
                                                        }

                                                        editor("editor", false) {

                                                            basePlugin { }
                                                            headingPlugin { }
                                                            listPlugin { }
                                                            linkPlugin { }
                                                            imagePlugin { }

                                                            button("save") {
                                                                className { "material-icons" }
                                                            }

                                                            subscribeBidirectional(model.editor, valueProperty)
                                                            subscribeBidirectional(model.editable, editable)
                                                        }

                                                    }

                                                    likeButton {
                                                        model(item.data.likes, item.data.links)
                                                    }

                                                    commentsSection {
                                                        model(item.data)
                                                    }

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }

                    input("newComment") {
                        style {
                            margin = "12px"
                            padding = "12px"
                            width = "calc(100% - 48px)"
                            backgroundColor = "var(--color-background-secondary)"
                            fontSize = "24px"
                            borderRadius = "8px"
                        }

                        placeholder = "Neuer Kommentar..."
                        onClick {
                            val lastItem = provider.getOrNull(provider.items.size - 1)
                            if (lastItem!!.data is FirstComment) {
                                if (! lastItem.data.editable.get()) {
                                    val firstComment = FirstComment()
                                    firstComment.editable.set(true)
                                    firstComment.user = Property(ApplicationService.app.get().user)
                                    provider.upsert(Data(firstComment))
                                }
                            } else {
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
    }
}

context(scope: NodeScope)
fun issuePage(block: context(NodeScope) IssuePage.() -> Unit = {}): IssuePage {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("issue-page")
    val c = IssuePage(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

    scope.ui.build.afterBuild {
        with(childScope) {
            c.afterBuild()
        }
    }

    block(childScope, c)

    return c
}
