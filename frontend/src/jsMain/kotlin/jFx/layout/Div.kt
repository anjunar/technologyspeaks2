package jFx.layout

import jFx.core.DSL
import jFx.core.DSL.ParentScope

class Div(override val ctx: DSL.BuildContext) : AbstractBox("") {
    companion object {
        fun ParentScope.div(body: Div.(DSL.BuildContext) -> Unit): Div {
            val builder = Div(this.ctx)
            addNode(builder, body)
            return builder
        }
    }
}

