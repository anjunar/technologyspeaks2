package app.components.timeline

import app.domain.core.Data
import app.domain.shared.OwnerProvider
import app.domain.timeline.Post
import jFx2.controls.*
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.style
import jFx2.core.template
import jFx2.layout.div
import jFx2.layout.hbox
import jFx2.router.navigateByRel
import jFx2.state.Property
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.w3c.dom.HTMLDivElement
import kotlin.time.Clock

class PostHeader(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    private val model = Property<OwnerProvider>(Post())

    private var onDelete: (() -> Unit)? = null
    private var onUpdate: (() -> Unit)? = null

    fun model(value: OwnerProvider) {
        model.set(value)
    }

    fun onDelete(fn: () -> Unit) {
        onDelete = fn
    }

    fun onUpdate(fn: () -> Unit) {
        onUpdate = fn
    }

    context(scope: NodeScope)
    fun afterBuild() {
        template {
            hbox {

                style {
                    columnGap = "10px"
                    alignItems = "center"
                }

                if (model.get().user == null) {
                    div {
                        style {
                            fontSize = "48px"
                        }
                        className { "material-icons" }
                        text("account_circle")
                    }
                } else {
                    image {
                        style {
                            height = "48px"
                            width = "48px"
                        }
                        src = model.get().user!!.get().image.get()?.thumbnailLink()!!
                    }
                }

                hbox {

                    style {
                        alignItems = "center"
                    }

                    hbox {

                        style {
                            columnGap = "6px"
                            alignItems = "baseline"
                        }


                        if (model.get().user == null) {
                            heading(3) {
                                text("User")
                            }
                        } else {
                            heading(3) {
                                text(model.get().user!!.get().nickName.get())
                            }
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

                        span {
                            style {
                                fontSize = "10px"
                            }
                            text(timeAgo(model.get().created.get()))
                        }
                    }

                }

                div {
                    style {
                        flex = "1"
                    }
                }

                navigateByRel("read", model.get().links) { navigate ->
                    button("edit") {
                        type("button")
                        className { "material-icons" }
                        onClick {
                            navigate()
                        }
                    }
                }

                navigateByRel("update", model.get().links) { navigate ->
                    button("edit") {
                        type("button")
                        className { "material-icons" }
                        onClick {
                            if (onUpdate == null) {
                                navigate()
                            } else {
                                onUpdate!!()
                            }
                        }
                    }
                }

                navigateByRel("delete", model.get().links) { navigate ->
                    button("delete") {
                        type("button")
                        className { "material-icons" }
                        onClick {
                            if (onDelete == null) {
                                navigate()
                            } else {
                                onDelete!!()
                            }

                        }
                    }
                }

            }
        }
    }

}

context(scope: NodeScope)
fun postHeader(block: context(NodeScope) PostHeader.() -> Unit = {}): PostHeader {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("post-header")
    val c = PostHeader(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

    block(childScope, c)

    with(childScope) {
        scope.ui.build.afterBuild { c.afterBuild() }
    }

    return c
}
