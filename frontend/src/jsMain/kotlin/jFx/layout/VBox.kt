package jFx.layout

import jFx.core.DSL
import jFx.core.DSL.ParentScope
import org.w3c.dom.HTMLDivElement

class VBox(override val ctx: DSL.BuildContext) : AbstractBox("vbox") {

    override fun build(): HTMLDivElement = node

    companion object {
        fun ParentScope.vbox(body: VBox.(DSL.BuildContext) -> Unit): VBox {
            val builder = VBox(this.ctx)
            addNode(builder, body)
            return builder
        }
    }
}

