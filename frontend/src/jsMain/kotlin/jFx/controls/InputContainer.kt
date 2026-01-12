package jFx.controls

import jFx.core.AbstractComponent
import jFx.core.DSL
import jFx.core.DSL.ChildNodeBuilder
import jFx.core.DSL.ElementBuilder
import jFx.core.DSL.ParentScope
import jFx.core.DSL.className
import jFx.core.DSL.component
import jFx.core.DSL.conditionReader
import jFx.core.DSL.render
import jFx.core.DSL.style
import jFx.layout.Div
import jFx.layout.Div.Companion.div
import jFx.layout.HorizontalLine
import jFx.layout.HorizontalLine.Companion.hr
import jFx.layout.Span
import jFx.layout.Span.Companion.span
import jFx.layout.VBox
import jFx.layout.VBox.Companion.vbox
import jFx.state.ListProperty
import jFx.state.Operators
import jFx.state.Operators.bind
import jFx.state.Property
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

class InputContainer(override val ctx: DSL.BuildContext) : AbstractComponent(), ChildNodeBuilder<HTMLDivElement, HTMLElement> {

    private val slot = Property<Input>(null)

    private lateinit var errors : Div

    private lateinit var label : Div

    private lateinit var horizontalLine: HorizontalLine

    val isEmptyProperty = Property(false)

    val placeholderProperty = Property("")

    val node: VBox by lazy {

        component {
            vbox {

                className = "input-container"

                this@InputContainer.label = div {

                    className = "input-label"

                    style {
                        height = "10px"
                        marginBottom = "5px"
                    }

                    conditionReader({ this@InputContainer.isEmptyProperty.get()!! }) {
                        span {
                            style {
                                fontSize = "xx-small"
                            }
                            textReader { this@InputContainer.placeholderProperty.get()!! }
                        }
                    }
                }

                render(this@InputContainer.slot)

                this@InputContainer.horizontalLine = hr {
                    className = "input-separator"
                }

                div {
                    this@InputContainer.errors = div {
                        className = "input-errors"
                    }
                }
            }
        }
    }

    var placeholder: String
        get() = placeholderProperty.get()!!
        set(value) { placeholderProperty.set(value) }

    override val children: ListProperty<ElementBuilder<*>> = ListProperty(emptyList())

    override fun add(child: ElementBuilder<*>) {
        children.setAll(children.get() + child)
    }

    override fun build(): HTMLDivElement = node.build()

    override fun afterBuild() {

        children.get().find { it is Input }?.let {
            val input = it as Input
            slot.set(input)

            input.placeholder = placeholder

            input.valueProperty.observe {
                node.ctx.invalidate()
            }

            input.statusProperty.observeChanges { change ->
                when (change) {
                    is ListProperty.Change.Add -> {
                        val status = change.items.first()
                        label.node.classList.add(status.name)
                        horizontalLine.node.classList.add(status.name)
                        if (status == Input.Companion.Status.empty) isEmptyProperty.set(false)
                    }
                    is ListProperty.Change.Remove -> {
                        val status = change.items.first()
                        label.node.classList.remove(status.name)
                        horizontalLine.node.classList.remove(status.name)
                        if (status == Input.Companion.Status.empty) isEmptyProperty.set(true)
                    }
                    else -> {}
                }
            }

            input.errorsProperty.observe {
                val errorSpans = it?.map { e -> span {
                    text = e
                    style {
                        color = "var(--color-error)"
                        fontSize = "xx-small"
                    }
                } }
                errors.children.setAll(errorSpans ?: emptyList())
            }
        }
    }

    companion object  {
        fun ParentScope.inputContainer(body: InputContainer.(DSL.BuildContext) -> Unit): InputContainer {
            val builder = InputContainer(this.ctx)
            addNode(builder, body)
            return builder
        }
    }
}

