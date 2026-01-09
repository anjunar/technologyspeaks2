package jFx.layout

import jFx.core.DSL
import jFx.core.DSL.ParentScope

class VBox(override val ctx: DSL.BuildContext) : AbstractBox("vbox") {

    companion object {
        fun ParentScope.vbox(body: VBox.(DSL.BuildContext) -> Unit): VBox {
            val builder = VBox(this.ctx)
            addNode(builder, body)
            return builder
        }
    }
}

