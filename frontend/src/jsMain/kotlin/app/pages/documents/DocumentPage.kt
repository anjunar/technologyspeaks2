package app.pages.documents

import app.domain.core.Data
import app.domain.core.Table
import app.domain.documents.Document
import jFx2.client.JsonClient
import jFx2.controls.button
import jFx2.controls.image
import jFx2.controls.span
import jFx2.controls.text
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.style
import jFx2.core.dsl.subscribeBidirectional
import jFx2.core.rendering.observeRender
import jFx2.core.template
import jFx2.forms.editor
import jFx2.forms.editor.plugins.basePlugin
import jFx2.forms.editor.plugins.headingPlugin
import jFx2.forms.editor.plugins.imagePlugin
import jFx2.forms.editor.plugins.listPlugin
import jFx2.forms.editor.plugins.linkPlugin
import jFx2.forms.form
import jFx2.forms.input
import jFx2.layout.div
import jFx2.layout.hbox
import jFx2.layout.vbox
import jFx2.router.PageInfo
import jFx2.router.navigateByRel
import jFx2.state.JobRegistry
import jFx2.state.Property
import jFx2.table.ComponentCell
import jFx2.table.DataProvider
import jFx2.table.LazyTableModel
import jFx2.table.SortState
import jFx2.table.TableCell
import jFx2.table.TextCell
import jFx2.table.tableView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.DateTimeFormatBuilder
import kotlinx.datetime.toInstant
import org.w3c.dom.HTMLDivElement
import org.w3c.fetch.RequestInit
import kotlin.time.Clock

class DocumentsProvider : DataProvider<Data<Document>> {
    override val totalCount = Property<Int?>(100_000)
    override val sortState: Property<SortState?> = Property(null)

    override suspend fun loadRange(offset: Int, limit: Int): List<Data<Document>> {
        val table = JsonClient.invoke<Table<Data<Document>>>("/service/document/documents?index=${offset}&limit=$limit&sort=created:desc")

        totalCount.set(table.size)

        return table
            .rows
    }
}

class DocumentPage(override var node: HTMLDivElement) : Component<HTMLDivElement>(), PageInfo {

    override val name: String = "Document"
    override val width: Int = -1
    override val height: Int = -1
    override val resizable: Boolean = true
    override var close: () -> Unit = {}

    private val model = Property(Data(Document()))

    fun model(value : Data<Document>) {
        model.set(value)
    }

    fun timeAgo(dateTime: LocalDateTime, clock: Clock = Clock.System): String {
        val now = clock.now()
        val zone = TimeZone.currentSystemDefault()

        val createdInstant = dateTime.toInstant(zone)
        val duration = now - createdInstant

        val hours = duration.inWholeHours
        val days = duration.inWholeDays

        return when {
            days > 0 -> "vor $days Tagen"
            hours > 0 -> "vor $hours Stunden"
            else -> "vor ${duration.inWholeMinutes} Minuten"
        }
    }

    context(scope: NodeScope)
    fun afterBuild() {

        val provider = DocumentsProvider()
        val job = SupervisorJob()
        val cs = CoroutineScope(job)
        onDispose { job.cancel() }
        val tableModel = LazyTableModel(cs, provider, pageSize = 200, prefetchPages = 2)


        template {
            style {
                height = "100%"
                width = "100%"
            }

            hbox {
                style {
                    height = "100%"
                    width = "100%"
                }

                vbox {

                    style {
                        width = "300px"
                        borderRight = "1px solid rgba(45,45,45,0.2)"
                    }

                    input("search", "search") {
                        placeholder = "Suche"
                        style {
                            fontSize = "32px"
                            padding = "24px"
                        }
                    }

                    tableView(tableModel, rowHeightPx = 64, headerVisible = false) {

                        columnProperty("title", "Titel", 300, valueProperty = { it.data.title }) {
                            ComponentCell(
                                outerScope = scope,
                                node = scope.create("div"),
                                render = { row, idx, v ->
                                    template {
                                        hbox {

                                            style {
                                                alignItems = "center"
                                                columnGap = "6px"
                                            }

                                            span {
                                                text("■")
                                            }

                                            vbox {
                                                div {
                                                    text(v!!)
                                                }
                                                div {

                                                    style {
                                                        fontSize = "12px"
                                                    }

                                                    text(timeAgo(row.data.created.get()))
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        onRowDoubleClick { document, _ ->
                            model.set(document)
                        }
                    }

                    button("Neues Dokument...") {
                        style {
                            marginBottom = "32px"
                        }
                        type("button")
                        onClick {
                            val document = Document()
                            document.editable.set(true)
                            model.set(Data(document))
                        }
                    }
                }

                observeRender(model) { observedModel ->
                    form(model = observedModel.data, clazz = Document::class) {

                        onSubmit {
                            val updateLink = this@form.model.links.find { it.rel == "update" }

                            if (updateLink == null) {
                                JsonClient.post("/service/document/documents/document", this@form.model)
                            } else {
                                JsonClient.invoke(updateLink, this@form.model)
                            }
                        }

                        style {
                            flex = "1"
                            height = "100%"
                            display = "flex"
                            flexDirection = "column"
                        }

                        hbox {
                            input("title") {

                                style {
                                    width = "calc(100% - 48px)"
                                    fontSize = "32px"
                                    padding = "24px"
                                    backgroundColor = "rgba(0,0,0,0.05)"
                                }

                                subscribeBidirectional(model.title, valueProperty)
                            }

                            button("edit") {

                                type("button")

                                style {
                                    fontSize = "32px"
                                    backgroundColor = "rgba(0,0,0,0.05)"
                                }

                                className { "material-icons" }

                                onClick { model.editable.set(!model.editable.get()) }

                            }
                        }

                        editor("editor", model.editable.get()) {

                            style {
                                flex = "1"
                                minHeight = "0"
                                padding = "12px"
                                border = "12px solid rgba(0,0,0,0.05)"
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
                            subscribeBidirectional(model.editable, editable)

                        }
                    }
                }


                vbox {

                    style {
                        width = "300px"
                        borderLeft = "1px solid rgba(45,45,45,0.2)"
                    }

                    span {
                        style {
                            fontSize = "32px"
                            padding = "24px"
                        }
                        text { "Issues" }
                    }

                }

            }
        }


    }
}

context(scope: NodeScope)
fun documentPage(block: context(NodeScope) DocumentPage.() -> Unit = {}): DocumentPage {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("document-page")
    val c = DocumentPage(el)
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
