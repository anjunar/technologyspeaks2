package jFx.layout

import jFx.core.DSL
import jFx.core.DSL.ParentScope
import org.w3c.dom.HTMLDivElement

class HBox(override val ctx: DSL.BuildContext) : AbstractBox("hbox") {

    override fun build(): HTMLDivElement = node

    companion object {
        fun ParentScope.vbox(body: HBox.(DSL.BuildContext) -> Unit): HBox {
            val builder = HBox(this.ctx)
            addNode(builder, body)
            return builder
        }
    }
}

