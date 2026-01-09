package jFx.controls

import jFx.core.DSL.LifeCycle
import jFx.core.DSL.NodeBuilder
import jFx.core.DSL.ParentScope
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event

class Input : NodeBuilder<HTMLInputElement> {

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

    override fun build(): HTMLInputElement = node

    override val applyValues: MutableList<() -> Unit> = mutableListOf()

    override var lifeCycle: LifeCycle = LifeCycle.Build

    companion object {
        fun ParentScope.input(body: Input.() -> Unit): Input {
            val builder = Input()
            addNode(builder, body)
            return builder
        }
    }
}

