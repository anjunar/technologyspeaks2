package app.pages.documents

import app.domain.core.Data
import app.domain.documents.Issue
import app.domain.documents.IssueCreated
import app.domain.documents.IssueUpdated
import app.services.ApplicationService
import jFx2.client.JsonClient
import jFx2.controls.button
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
import jFx2.forms.form
import jFx2.forms.input
import jFx2.forms.inputContainer
import jFx2.layout.vbox
import jFx2.router.PageInfo
import jFx2.state.Property
import org.w3c.dom.HTMLDivElement

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
