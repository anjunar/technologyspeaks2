package jFx.layout

import jFx.core.DSL.LifeCycle
import jFx.core.DSL.NodeBuilder
import jFx.core.DSL.ParentScope
import kotlinx.browser.document
import org.w3c.dom.HTMLSpanElement

class Span : NodeBuilder<HTMLSpanElement> {

    val node by lazy {
        document.createElement("span") as HTMLSpanElement
    }

    var text: String
        get() = read(node.textContent ?: "")
        set(value) = write { node.textContent = value }


    override fun build(): HTMLSpanElement = node

    override val applyValues: MutableList<() -> Unit> = mutableListOf()

    override var lifeCycle: LifeCycle = LifeCycle.Build

    companion object {
        fun ParentScope.span(body: Span.() -> Unit): Span {
            val builder = Span()
            addNode(builder, body)
            return builder
        }
    }

}

