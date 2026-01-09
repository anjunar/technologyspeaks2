package jFx.layout

import jFx.core.DSL.ChildNodeBuilder
import jFx.core.DSL.ElementBuilder
import jFx.core.DSL.LifeCycle
import jFx.core.DSL.ParentScope
import jFx.state.ListProperty
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.Node

class Div : ChildNodeBuilder<HTMLDivElement, Node> {
    val node by lazy { document.createElement("div") as HTMLDivElement }

    override val children = ListProperty<ElementBuilder<*>>()

    override fun build(): HTMLDivElement = node

    override fun add(child: ElementBuilder<*>) {
        val built = child.build()
        if (built is Node) {
            node.appendChild(built)
        }
    }

    override val applyValues = mutableListOf<() -> Unit>()
    override var lifeCycle = LifeCycle.Build

    companion object {
        fun ParentScope.div(body: Div.() -> Unit): Div {
            val builder = Div()
            addNode(builder, body)
            return builder
        }
    }
}

