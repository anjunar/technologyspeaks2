package jFx.controls

import jFx.core.AbstractComponent
import jFx.core.DSL.ChildNodeBuilder
import jFx.core.DSL.ElementBuilder
import jFx.core.DSL.component
import jFx.core.DSL.ParentScope
import jFx.core.DSL.style
import jFx.layout.Div
import jFx.layout.Div.Companion.div
import jFx.layout.Span.Companion.span
import jFx.layout.VBox.Companion.vbox
import jFx.state.ListProperty
import jFx.state.Operators.bind
import jFx.state.Property
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

class InputContainer : AbstractComponent(), ChildNodeBuilder<HTMLDivElement, HTMLElement> {

    private lateinit var slot: Div

    val placeholderProperty = Property("")

    val node: HTMLDivElement by lazy {

        component {
            vbox {
                span {
                    style {
                        fontSize = "xx-small"
                        color = "gray"
                    }
                    textProperty.bind(this@InputContainer.placeholderProperty)
                }

                this@InputContainer.slot = div {}
            }
        }
    }

    var placeholder: String
        get() = placeholderProperty.get()
        set(value) { placeholderProperty.set(value) }

    override val children: ListProperty<ElementBuilder<*>> = ListProperty(emptyList())

    override fun add(child: ElementBuilder<*>) {
        children.set(emptyList<ElementBuilder<*>>() + child)

        write {
            slot.add(child)
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

