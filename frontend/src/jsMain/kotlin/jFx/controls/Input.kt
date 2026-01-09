package jFx.controls

import jFx.core.AbstractComponent
import jFx.core.DSL.NodeBuilder
import jFx.core.DSL.ParentScope
import jFx.state.Property
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener

class Input : AbstractComponent(), NodeBuilder<HTMLInputElement> {

    val node by lazy {
        document.createElement("input") as HTMLInputElement
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
                if (input.value != v) input.value = v
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
        fun ParentScope.input(body: Input.() -> Unit): Input {
            val builder = Input()
            addNode(builder, body)
            return builder
        }
    }
}

