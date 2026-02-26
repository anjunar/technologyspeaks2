@file:OptIn(ExperimentalUuidApi::class)

package app.components.commentable

import app.components.likeable.likeButton
import app.components.timeline.postHeader
import app.domain.core.Data
import app.domain.shared.FirstComment
import app.domain.shared.SecondComment
import app.services.ApplicationService
import jFx2.client.JsonClient
import jFx2.controls.button
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.renderComponent
import jFx2.core.dsl.style
import jFx2.core.dsl.subscribeBidirectional
import jFx2.core.rendering.condition
import jFx2.core.rendering.foreach
import jFx2.core.rendering.observeRender
import jFx2.core.template
import jFx2.forms.editor
import jFx2.forms.editor.plugins.*
import jFx2.forms.form
import jFx2.forms.input
import jFx2.layout.div
import jFx2.layout.vbox
import jFx2.router.navigate
import jFx2.state.JobRegistry
import jFx2.state.Property
import kotlinx.browser.window
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
                vbox {

                    style {
                        alignItems = "flex-end"
                    }

                    button("Kommentieren") {
                        type("button")
                        onClick {
                            val lastComment = commentable.get().comments.lastOrNull()
                            if (lastComment != null && lastComment.editable.get()) return@onClick
                            val secondComment = SecondComment()
                            secondComment.id = Property(Uuid.generateV4().toString())
                            secondComment.editable.set(true)
                            commentable.get().comments.add(secondComment)
                        }

                    }
                }
            }

            observeRender(commentable) { commentable ->
                foreach(commentable.comments, {key -> key.id!!.get()}) { comment, index ->

                    form(model = comment, clazz = SecondComment::class) {

                        vbox {
                            onSubmit {
                                busy.set(true)
                                try {
                                    JsonClient.put<FirstComment, Data<FirstComment>>("/service" + createLink.url, this@CommentsSection.commentable.get())
                                    comment.user = Property(ApplicationService.app.get().user)
                                    comment.editable.set(false)
                                } finally {
                                    busy.set(false)
                                }
                            }

                            className { "glass-border" }

                            postHeader {
                                model(this@form.model)

                                onDelete {
                                    JobRegistry.instance.launch("Comment Remove", "Comment") {
                                        val updateLink = comment.links.find { it.rel == "update" }
                                        commentable.comments.remove(comment)
                                        JsonClient.put<FirstComment, Data<FirstComment>>("/service" + updateLink!!.url, commentable)
                                    }
                                }

                                onUpdate {
                                    val editable = this@form.model.editable
                                    editable.set(! editable.get())
                                }

                            }

                            editor("editor", false) {

                                style {
                                    flex = "1"
                                    minHeight = "0px"
                                }

                                basePlugin { }
                                headingPlugin { }
                                listPlugin { }
                                linkPlugin { }
                                imagePlugin { }

                                button("save") {
                                    className { "material-icons hover" }
                                }

                                subscribeBidirectional(this@form.model.editor, valueProperty)
                                subscribeBidirectional(this@form.model.editable, editable)
                            }

                            likeButton {
                                model(this@form.model.likes, this@form.model.links)
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
