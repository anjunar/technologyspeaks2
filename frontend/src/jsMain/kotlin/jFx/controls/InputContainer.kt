package jFx.controls

import jFx.core.AbstractComponent
import jFx.core.DSL.ChildNodeBuilder
import jFx.core.DSL.ElementBuilder
import jFx.core.DSL.component
import jFx.core.DSL.ParentScope
import jFx.core.DSL.condition
import jFx.core.DSL.render
import jFx.core.DSL.style
import jFx.layout.Span.Companion.span
import jFx.layout.VBox.Companion.vbox
import jFx.state.ListProperty
import jFx.state.Operators
import jFx.state.Operators.bind
import jFx.state.Property
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

class InputContainer : AbstractComponent(), ChildNodeBuilder<HTMLDivElement, HTMLElement> {

    private val slot = Property<Input>(null)

    private val isEmptyProperty = Property(false)

    val placeholderProperty = Property("")

    private var emptyBindingDispose: (() -> Unit)? = null

    val node: HTMLDivElement by lazy {

        slot.observe { input ->
            emptyBindingDispose?.invoke()
            emptyBindingDispose = null

            if (input != null) {
                input.placeholder = placeholder

                emptyBindingDispose = Operators.computed(
                    source = input.valueProperty,
                    target = isEmptyProperty
                ) { value: String ->
                    ! value.isBlank()
                }
            } else {
                isEmptyProperty.set(false)
            }
        }

        component {
            vbox {

                condition(this@InputContainer.isEmptyProperty) {
                    span {
                        style {
                            fontSize = "xx-small"
                            color = "gray"
                        }
                        val disposable = textProperty.bind(this@InputContainer.placeholderProperty)
                        onDispose { disposable.invoke() }
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

    override fun build(): HTMLDivElement = node

    companion object  {
        fun ParentScope.inputContainer(body: InputContainer.() -> Unit): InputContainer {
            val builder = InputContainer()
            addNode(builder, body)
            return builder
        }
    }
}

