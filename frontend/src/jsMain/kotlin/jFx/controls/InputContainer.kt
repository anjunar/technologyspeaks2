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

    private val isEmptyProperty = Property(false)

    val placeholderProperty = Property("")

    val node: VBox by lazy {

        slot.observe { input ->

            if (input != null) {
                input.placeholder = placeholder

                Operators.computed(
                    source = input.valueProperty,
                    target = isEmptyProperty
                ) { value: String ->
                    ! value.isBlank()
                }

                val inputSub = input.valueProperty.observe {
                    node.ctx.invalidate()
                }

                onDispose { inputSub() }

            } else {
                isEmptyProperty.set(false)
            }
        }

        component {
            vbox {

                conditionReader({ this@InputContainer.isEmptyProperty.get()!! }) {
                    span {
                        style {
                            fontSize = "xx-small"
                            color = "gray"
                        }
                        textReader { this@InputContainer.placeholderProperty.get()!! }
                    }
                }

                render(this@InputContainer.slot)
            }
        }
    }

    var placeholder: String
        get() = placeholderProperty.get()!!
        set(value) { placeholderProperty.set(value) }

    override val children: ListProperty<ElementBuilder<*>> = ListProperty(emptyList())

    override fun add(child: ElementBuilder<*>) {
        children.set(emptyList<ElementBuilder<*>>() + child)

        write {
            slot.set(child as Input)
        }
    }

    override fun build(): HTMLDivElement = node.build()

    companion object  {
        fun ParentScope.inputContainer(body: InputContainer.(DSL.BuildContext) -> Unit): InputContainer {
            val builder = InputContainer(this.ctx)
            addNode(builder, body)
            return builder
        }
    }
}

