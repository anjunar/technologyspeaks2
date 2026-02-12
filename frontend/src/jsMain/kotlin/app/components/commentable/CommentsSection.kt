@file:OptIn(ExperimentalUuidApi::class)

package app.components.commentable

import app.components.timeline.postHeader
import app.domain.core.Data
import app.domain.shared.FirstComment
import app.domain.shared.SecondComment
import jFx2.client.JsonClient
import jFx2.controls.button
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.style
import jFx2.core.dsl.subscribeBidirectional
import jFx2.core.rendering.condition
import jFx2.core.rendering.foreach
import jFx2.core.rendering.observeRender
import jFx2.core.template
import jFx2.forms.editor
import jFx2.forms.editor.plugins.*
import jFx2.forms.form
import jFx2.layout.div
import jFx2.layout.vbox
import jFx2.state.Property
import org.w3c.dom.HTMLDivElement
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CommentsSection(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    private var createRel: String = "update"
    private val commentable = Property(FirstComment())

    fun model(value : FirstComment) {
        this.commentable.set(value)
    }

    context(scope: NodeScope)
    fun afterBuild() {
        val createLink = commentable.get().links.get().firstOrNull { it.rel == createRel } ?: return

        val busy = Property(false)

        template {

            div {
                button("Kommentieren") {
                    type("button")
                    onClick {
                        val secondComment = SecondComment()
                        secondComment.id = Property(Uuid.generateV4().toString())
                        secondComment.editable.set(true)
                        commentable.get().comments.add(secondComment)
                    }
                }
            }

            observeRender(commentable) { commentable ->
                foreach(commentable.comments, {key -> key.id!!.get()}) { comment, index ->

                    condition(comment.editable) {
                        then {
                            form(model = comment, clazz = SecondComment::class) {
                                style {
                                    height = "300px"
                                }

                                onSubmit {
                                    busy.set(true)
                                    try {
                                        val created = JsonClient.put<FirstComment, Data<FirstComment>>("/service" + createLink.url, this@CommentsSection.commentable.get())
                                        this@CommentsSection.commentable.set(created.data)
                                    } finally {
                                        busy.set(false)
                                    }
                                }

                                vbox {
                                    editor("editor") {

                                        style {
                                            flex = "1"
                                            minHeight = "0px"
                                        }

                                        basePlugin { }
                                        headingPlugin { }
                                        listPlugin { }
                                        linkPlugin { }
                                        imagePlugin { }

                                        subscribeBidirectional(this@form.model.editor, valueProperty)
                                    }


                                    button("send") {
                                        className { "material-icons hover" }
                                    }
                                }

                            }
                        }
                        elseDo {
                            form(model = comment, clazz = SecondComment::class) {

                                className { "glass-border" }

                                postHeader {
                                    model(Data(this@form.model))

                                    onDelete {
                                        commentable.comments.remove(comment)
                                    }
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
