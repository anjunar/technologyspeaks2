package jFx.controls

import jFx.core.DSL.ChildNodeBuilder
import jFx.core.DSL.ElementBuilder
import jFx.core.DSL.component
import jFx.core.DSL.LifeCycle
import jFx.core.DSL.ParentScope
import jFx.layout.Div
import jFx.layout.Div.Companion.div
import jFx.layout.Span.Companion.span
import jFx.layout.VBox.Companion.vbox
import jFx.state.ListProperty
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

class InputContainer : ChildNodeBuilder<HTMLDivElement, HTMLElement> {

    private lateinit var slot: Div

    val node: HTMLDivElement by lazy {

        component {
            vbox {
                span { text = "Input:" }

                this@InputContainer.slot = div {}
            }
        }
    }

    override val children: ListProperty<ElementBuilder<*>> = ListProperty(emptyList())

    override fun add(child: ElementBuilder<*>) {
        children.set(children.get() + child)

        write {
            slot.add(child)
        }
    }

    override fun build(): HTMLDivElement = node

    override val applyValues: MutableList<() -> Unit> = mutableListOf()
    override var lifeCycle: LifeCycle = LifeCycle.Build

    companion object  {
        fun ParentScope.inputContainer(body: InputContainer.() -> Unit): InputContainer {
            val builder = InputContainer()
            addNode(builder, body)
            return builder
        }
    }
}

