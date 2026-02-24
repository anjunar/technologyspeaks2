package app.pages.documents

import app.components.likeable.likeButton
import app.components.timeline.postHeader
import app.domain.core.Data
import app.domain.core.Table
import app.domain.documents.Document
import app.domain.documents.Issue
import app.domain.timeline.Post
import jFx2.client.JsonClient
import jFx2.controls.button
import jFx2.controls.heading
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
import jFx2.router.navigate
import jFx2.router.navigateByRel
import jFx2.state.JobRegistry
import jFx2.state.Property
import jFx2.table.ComponentCell
import jFx2.table.DataProvider
import jFx2.table.LazyTableModel
import jFx2.table.SelectionMode
import jFx2.table.SortState
import jFx2.table.tableView
import jFx2.virtual.RangeDataProvider
import jFx2.virtual.virtualList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.w3c.dom.HTMLDivElement
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

class IssuesRangeProvider(override val maxItems: Int = 5000, override val pageSize: Int = 50,val document: Document) : RangeDataProvider<Data<Issue>>() {

    override suspend fun fetch(index: Int, limit: Int): Table<Data<Issue>> {
        return JsonClient.invoke<Table<Data<Issue>>>("/service/document/documents/document/${document.id!!.get()}/issues?index=${items.size}&limit=$limit&sort=created:desc")
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

        val issuesProvider = IssuesRangeProvider(document = model.get().data)
        val provider = DocumentsProvider()
        val job = SupervisorJob()
        val cs = CoroutineScope(job)
        onDispose { job.cancel() }
        val tableModel = LazyTableModel(cs, provider, pageSize = 200, prefetchPages = 2)


        template {
            style {
                height = "100%"
                width = "100%"
                setProperty("overflow", "hidden")
            }

            hbox {
                node.classList.add("documents-layout")
                style {
                    height = "100%"
                    width = "100%"
                }

                vbox {
                    node.classList.add("doc-panel")

                    style {
                        width = "320px"
                        minWidth = "280px"
                        maxWidth = "360px"
                    }

                    hbox {
                        node.classList.add("doc-panel-header")

                        span {
                            node.classList.add("doc-panel-title")
                            text("Dokumente")
                        }
                    }

                    div {
                        node.classList.add("doc-search")

                        span {
                            node.classList.add("material-icons")
                            text("search")
                        }

                        input("search", "search") {
                            placeholder = "Suche..."
                        }
                    }

                    val docsTable = tableView(tableModel, rowHeightPx = 64, headerVisible = false) {

                        columnProperty("title", "Titel", 300, valueProperty = { it.data.title }) {
                            ComponentCell(
                                outerScope = scope,
                                node = scope.create("div"),
                                render = { row, idx, v ->
                                    template {
                                        hbox {

                                            style {
                                                alignItems = "center"
                                                columnGap = "10px"
                                                width = "100%"
                                            }

                                            span {
                                                node.classList.add("material-icons")
                                                style {
                                                    fontSize = "18px"
                                                    opacity = "0.75"
                                                }
                                                text("description")
                                            }

                                            vbox {
                                                style {
                                                    setProperty("overflow", "hidden")
                                                }
                                                div {
                                                    style {
                                                        fontWeight = "600"
                                                        setProperty("overflow", "hidden")
                                                        textOverflow = "ellipsis"
                                                    }

                                                    val title = v?.ifBlank { "(Ohne Titel)" } ?: "(Ohne Titel)"
                                                    text(title)
                                                }
                                                div {

                                                    style {
                                                        fontSize = "12px"
                                                        opacity = "0.75"
                                                    }

                                                    text(timeAgo(row.data.created.get()))
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        onSelectionChanged { selected ->
                            selected.firstOrNull()?.let { model.set(it) }
                        }

                        onRowDoubleClick { document, _ ->
                            model.set(document)
                        }
                    }

                    docsTable.node.classList.add("doc-table")
                    docsTable.selectionModel.mode.set(SelectionMode.SINGLE)

                    button("") {
                        node.classList.add("doc-new-btn")
                        style {
                            marginBottom = "12px"
                            display = "flex"
                            alignItems = "center"
                            justifyContent = "center"
                            columnGap = "10px"
                        }
                        type("button")

                        span {
                            node.classList.add("material-icons")
                            style {
                                fontSize = "20px"
                                opacity = "0.85"
                            }
                            text("add")
                        }
                        span {
                            text("Neues Dokument")
                        }

                        onClick {
                            docsTable.selectionModel.clearSelection()
                            val document = Document()
                            document.editable.set(true)
                            model.set(Data(document))
                        }
                    }
                }

                observeRender(model) { observedModel ->
                    form(model = observedModel.data, clazz = Document::class) {
                        node.classList.add("doc-panel")

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
                            minWidth = "0"
                            height = "100%"
                            display = "flex"
                            flexDirection = "column"
                        }

                        hbox {
                            node.classList.add("doc-titlebar")
                            input("title") {

                                style {
                                    flex = "1"
                                    minWidth = "0"
                                }

                                placeholder = "Titel"
                                subscribeBidirectional(model.title, valueProperty)
                                onDispose(model.editable.observe { editable -> node.disabled = !editable })
                            }

                            button("edit") {

                                type("button")

                                onClick { model.editable.set(!model.editable.get()) }

                                node.classList.add("material-icons")
                                node.classList.add("doc-icon-btn")
                                onDispose(
                                    model.editable.observe { editable ->
                                        text(if (editable) "done" else "edit")
                                        if (editable) node.classList.add("active") else node.classList.remove("active")
                                    }
                                )
                            }
                        }

                        editor("editor", model.editable.get()) {
                            node.classList.add("doc-editor")

                            basePlugin { }
                            headingPlugin { }
                            listPlugin { }
                            linkPlugin { }
                            imagePlugin { }

                            button("save") {
                                node.classList.add("material-icons")
                                node.classList.add("doc-icon-btn")
                            }

                            subscribeBidirectional(model.editor, valueProperty)
                            subscribeBidirectional(model.editable, editable)

                        }
                    }
                }


                vbox {
                    node.classList.add("doc-panel")

                    style {
                        width = "320px"
                        minWidth = "280px"
                        maxWidth = "360px"
                    }

                    hbox {
                        node.classList.add("doc-panel-header")

                        span {
                            node.classList.add("doc-panel-title")
                            text("Aufgaben")
                        }
                    }

                    div {

                        style {
                            flex = "1"
                        }

                        virtualList(
                            dataProvider = issuesProvider,
                            estimateHeightPx = 44,
                            overscanPx = 240,
                            prefetchItems = 80,
                            renderer = { item, index ->

                                template {

                                    form(model = item?.data, clazz = Issue::class) {

                                        className { "glass-border" }

                                        heading(3) {
                                            text(model.title.get())
                                        }

                                        editor("editor", false) {
                                            basePlugin { }
                                            headingPlugin { }
                                            listPlugin { }
                                            linkPlugin { }
                                            imagePlugin { }

                                            subscribeBidirectional(this@form.model.editor, valueProperty)
                                        }

                                    }

                                }
                            })
                    }

                    button("") {
                        node.classList.add("doc-new-btn")
                        style {
                            marginBottom = "12px"
                            display = "flex"
                            alignItems = "center"
                            justifyContent = "center"
                            columnGap = "10px"
                        }
                        type("button")

                        span {
                            node.classList.add("material-icons")
                            style {
                                fontSize = "20px"
                                opacity = "0.85"
                            }
                            text("add")
                        }
                        span {
                            text("Neue Aufgabe")
                        }

                        onClick {
                            navigate("/document/documents/document/${model.get().data.id!!.get()}/issues/issue")
                        }
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
