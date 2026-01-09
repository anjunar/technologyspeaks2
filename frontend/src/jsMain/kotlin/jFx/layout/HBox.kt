package jFx.layout

import jFx.core.DSL
import jFx.core.DSL.ParentScope

class HBox(override val ctx: DSL.BuildContext) : AbstractBox("hbox") {
    companion object {
        fun ParentScope.vbox(body: HBox.(DSL.BuildContext) -> Unit): HBox {
            val builder = HBox(this.ctx)
            addNode(builder, body)
            return builder
        }
    }
}

