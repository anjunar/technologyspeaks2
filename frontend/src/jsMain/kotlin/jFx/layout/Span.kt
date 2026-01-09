package jFx.layout

import jFx.core.AbstractComponent
import jFx.core.DSL
import jFx.core.DSL.LifeCycle
import jFx.core.DSL.NodeBuilder
import jFx.core.DSL.ParentScope
import jFx.state.Property
import kotlinx.browser.document
import org.w3c.dom.HTMLSpanElement

class Span : AbstractComponent(), NodeBuilder<HTMLSpanElement> {

    val textProperty = Property("")

    val node by lazy {
        val spanElement = document.createElement("span") as HTMLSpanElement

        textProperty.observe { spanElement.textContent = it }

        spanElement
    }

    var text: String
        get() = read(node.textContent ?: "")
        set(value) = write { node.textContent = value }

    override fun build(): HTMLSpanElement = node

    companion object {
        fun ParentScope.span(body: Span.(DSL.BuildContext) -> Unit): Span {
            val builder = Span()
            addNode(builder, body)
            return builder
        }
    }

}

