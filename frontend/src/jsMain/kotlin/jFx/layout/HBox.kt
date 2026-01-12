package jFx.layout

import jFx.core.BuildContext
import jFx.core.ParentScope
import org.w3c.dom.HTMLDivElement

class HBox(override val ctx: BuildContext) : AbstractBox("hbox") {

    override fun build(): HTMLDivElement = node

    companion object {
        fun ParentScope.vbox(body: HBox.(BuildContext) -> Unit): HBox {
            val builder = HBox(this.ctx)
            addNode(builder, body)
            return builder
        }
    }
}

