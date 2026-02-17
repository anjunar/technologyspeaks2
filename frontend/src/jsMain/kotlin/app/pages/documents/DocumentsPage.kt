package app.pages.documents

import app.domain.core.Data
import app.domain.core.Table
import app.domain.core.User
import app.domain.documents.Document
import jFx2.client.JsonClient
import jFx2.controls.image
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.style
import jFx2.core.dsl.subscribeBidirectional
import jFx2.core.template
import jFx2.forms.editor
import jFx2.forms.editor.plugins.basePlugin
import jFx2.forms.editor.plugins.headingPlugin
import jFx2.forms.editor.plugins.imagePlugin
import jFx2.forms.editor.plugins.listPlugin
import jFx2.forms.editor.plugins.linkPlugin
import jFx2.forms.form
import jFx2.forms.input
import jFx2.forms.inputContainer
import jFx2.layout.hbox
import jFx2.layout.vbox
import jFx2.router.PageInfo
import jFx2.router.navigateByRel
import jFx2.state.Property
import jFx2.table.ComponentCell
import jFx2.table.DataProvider
import jFx2.table.LazyTableModel
import jFx2.table.SortState
import jFx2.table.TextCell
import jFx2.table.tableView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.w3c.dom.HTMLDivElement

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

class DocumentsPage(override var node: HTMLDivElement) : Component<HTMLDivElement>(), PageInfo {

    override val name: String = "Document"
    override val width: Int = -1
    override val height: Int = -1
    override val resizable: Boolean = true
    override var close: () -> Unit = {}

    private val model = Property(Data(Document()))

    fun model(value : Data<Document>) {
        model.set(value)
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
                    tableView(tableModel, rowHeightPx = 64, headerVisible = false) {

                        columnProperty(
                            id = "title",
                            header = "Titel",
                            prefWidthPx = 300,
                            valueProperty = { it.data.title },
                            cellFactory = {
                                val host = scope.create<HTMLDivElement>("div")
                                TextCell(host)
                            }
                        )

                        onRowDoubleClick { user, _ ->
                            navigateByRel("read", user.data.links) { navigate -> navigate() }
                        }
                    }
                }

                form(model = model.get().data, clazz = Document::class) {

                    style {
                        flex = "1"
                        height = "100%"
                        display = "flex"
                        flexDirection = "column"
                    }


                    input("title") {

                        style {
                            width = "calc(100% - 48px)"
                            fontSize = "32px"
                            padding = "24px"
                        }

                        subscribeBidirectional(model.title, valueProperty)
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

                        subscribeBidirectional(model.editor, valueProperty)

                    }


                }

            }
        }


    }
}

context(scope: NodeScope)
fun documentPage(block: context(NodeScope) DocumentsPage.() -> Unit = {}): DocumentsPage {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("document-page")
    val c = DocumentsPage(el)
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
