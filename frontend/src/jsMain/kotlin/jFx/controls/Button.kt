package jFx.controls

import jFx.core.DSL.LifeCycle
import jFx.core.DSL.NodeBuilder
import jFx.core.DSL.ParentScope
import jFx.state.Property
import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement

class Button() : NodeBuilder<HTMLButtonElement> {

    val node by lazy {
        val element = document.createElement("button") as HTMLButtonElement

        textProperty.observe { element.textContent = it }

        element
    }

    override fun build(): HTMLButtonElement {
        return node
    }

    val textProperty = Property("")

    var text: String
        get() = read(node.textContent ?: "")
        set(value) = write { node.textContent = value }

    fun onClick(handler: () -> Unit) = write {
        node.onclick = { handler(); null }
    }

    override val applyValues: MutableList<() -> Unit> = mutableListOf()

    override var lifeCycle: LifeCycle = LifeCycle.Build

    companion object {
        fun ParentScope.button(body: Button.() -> Unit): Button {
            val builder = Button()
            addNode(builder, body)
            return builder
        }
    }
}