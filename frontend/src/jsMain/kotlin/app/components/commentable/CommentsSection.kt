package app.components.commentable

import app.domain.core.Data
import app.domain.core.Link
import app.domain.core.Table
import app.domain.shared.Comment
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
import jFx2.core.rendering.condition
import jFx2.core.template
import jFx2.forms.editor
import jFx2.forms.editor.plugins.*
import jFx2.forms.editorView
import jFx2.forms.form
import jFx2.layout.div
import jFx2.layout.hbox
import jFx2.layout.vbox
import jFx2.state.ListProperty
import jFx2.state.Property
import jFx2.virtual.RangeDataProvider
import jFx2.virtual.virtualList
import org.w3c.dom.HTMLDivElement

class CommentsSection(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    private var links: ListProperty<Link>? = null
    private var listRel: String = "comments"
    private var createRel: String = "comment"
    private val editable = Property(false)

    fun model(
        links: ListProperty<Link>,
        listRel: String = "comments",
        createRel: String = "comment"
    ) {
        this.links = links
        this.listRel = listRel
        this.createRel = createRel
    }

    context(scope: NodeScope)
    fun afterBuild() {
        val links = links ?: return

        val createLink = links.get().firstOrNull { it.rel == createRel } ?: return

        val busy = Property(false)
        val newText = Property(Comment())

        template {

            div {
                button("Kommentieren") {
                    type("button")
                    onClick {
                        editable.set(!editable.get())
                    }
                }
            }

            condition(editable) {
                then {
                    vbox {
                        style {
                            setProperty("gap", "8px")
                            marginTop = "8px"
                        }

                        form(model = newText.get(), clazz = Comment::class) {
                            style {
                                height = "120px"
                            }

                            onSubmit {
                                busy.set(true)
                                try {
                                    val created = JsonClient.post<Comment, Data<Comment>>("/service" + createLink.url, this@form.model)
                                    this@form.model.editor.set("")
                                } finally {
                                    busy.set(false)
                                }
                            }

                            editor("editor") {
                                basePlugin { }
                                headingPlugin { }
                                listPlugin { }
                                linkPlugin { }
                                imagePlugin { }

                                subscribeBidirectional(this@form.model.editor, valueProperty)
                            }


                            button("send") {
                                className { "material-icons container hover" }
                            }
                        }

                    }
                }
            }
        }
    }
}

context(scope: NodeScope)
fun commentsSection(block: context(NodeScope) CommentsSection.() -> Unit = {}): CommentsSection {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("comments-section")
    val c = CommentsSection(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))
    block(childScope, c)

    with(childScope) {
        scope.ui.build.afterBuild { c.afterBuild() }
    }

    return c
}
