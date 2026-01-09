package jFx.controls

import jFx.core.AbstractComponent
import jFx.core.DSL
import jFx.core.DSL.ChildNodeBuilder
import jFx.core.DSL.ElementBuilder
import jFx.core.DSL.element
import jFx.core.DSL.ParentScope
import jFx.core.DSL.component
import jFx.core.DSL.condition
import jFx.core.DSL.conditionReader
import jFx.core.DSL.render
import jFx.core.DSL.style
import jFx.layout.Div
import jFx.layout.Div.Companion.div
import jFx.layout.HorizontalLine.Companion.hr
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

    private lateinit var slot : Div

    private lateinit var errors : Div

    private val isEmptyProperty = Property(false)

    val placeholderProperty = Property("")

    val node: VBox by lazy {

        component {
            vbox {

                div {

                    style {
                        height = "10px"
                        marginBottom = "5px"
                    }

                    conditionReader({ this@InputContainer.isEmptyProperty.get()!! }) {
                        span {
                            style {
                                fontSize = "xx-small"
                                color = "gray"
                            }
                            textReader { this@InputContainer.placeholderProperty.get()!! }
                        }
                    }
                }

                this@InputContainer.slot = div {}

                hr {
                    style {
                        margin = "0px"
                    }
                }

                div {

                    style {
                        height = "10px"
                    }

                    this@InputContainer.errors = div {}
                }
            }
        }
    }

    var placeholder: String
        get() = placeholderProperty.get()!!
        set(value) { placeholderProperty.set(value) }

    override val children: ListProperty<ElementBuilder<*>> = ListProperty(emptyList())

    override fun add(child: ElementBuilder<*>) {
        children.set(children.get() + child)
    }

    override fun build(): HTMLDivElement = node.build()

    override fun afterBuild() {

        children.get().find { it is Input }?.let {
            val input = it as Input
            slot.add(input)

            input.placeholder = placeholder

            Operators.computed(
                source = input.valueProperty,
                target = isEmptyProperty
            ) { value: String ->
                ! value.isBlank()
            }

            input.valueProperty.observe {
                node.ctx.invalidate()
            }

            input.errorsProperty.observe {
                val errorSpans = it?.map { e -> span {
                    text = e
                    style {
                        color = "var(--color-error)"
                        fontSize = "xx-small"
                    }
                } }
                errors.children.set(errorSpans ?: emptyList())
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

