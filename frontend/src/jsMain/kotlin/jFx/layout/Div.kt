package jFx.layout

import jFx.core.BuildContext
import jFx.core.ParentScope
import org.w3c.dom.HTMLDivElement

class Div(override val ctx: BuildContext) : AbstractBox("") {

    override fun build(): HTMLDivElement = node

    companion object {
        fun ParentScope.div(body: Div.(BuildContext) -> Unit): Div {
            val builder = Div(this.ctx)
            addNode(builder, body)
            return builder
        }
    }
}

