package jFx.controls

import jFx.core.AbstractComponent
import jFx.core.DSL
import jFx.core.DSL.NodeBuilder
import jFx.core.DSL.ParentScope
import jFx.state.Property
import jFx.util.EventHelper
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener

class Input(override val ctx: DSL.BuildContext): AbstractComponent(), NodeBuilder<HTMLInputElement> {

    val valueProperty = Property("")

    val node by lazy {
        val inputElement = document.createElement("input") as HTMLInputElement

        EventHelper.events(inputElement, {ctx.invalidate()}, "input", "blur", "focus")

        bind(valueProperty)

        inputElement
    }

    fun valueWriter(callback : (String) -> Unit) {
        node.addEventListener("input", { callback(node.value) })
    }

    var value: String
        get() = read(node.value)
        set(value) = write { node.value = value }

    var placeholder: String
        get() = read(node.placeholder)
        set(value) = write { node.placeholder = value }

    fun onClick(name : String, handler: (event : Event) -> Unit) = write {
        node.addEventListener(name, { handler(it) })
    }

    fun bind(property: Property<String>) {
        write {
            val input = build()

            val disposeObs = property.observe { v ->
                if (input.value != v) input.value = v!!
            }
            onDispose(disposeObs)

            val listener = EventListener {
                val v = input.value
                if (property.get() != v) property.set(v)
            }
            input.addEventListener("input", listener)
            onDispose { input.removeEventListener("input", listener) }
        }
    }

    override fun build(): HTMLInputElement = node

    companion object {
        fun ParentScope.input(body: Input.(DSL.BuildContext) -> Unit): Input {
            val builder = Input(ctx)
            addNode(builder, body)
            return builder
        }
    }
}

