package jFx.layout

import jFx.core.BuildContext
import jFx.core.ParentScope
import org.w3c.dom.HTMLDivElement

class VBox(override val ctx: BuildContext) : AbstractBox("vbox") {

    override fun build(): HTMLDivElement = node

    companion object {
        fun ParentScope.vbox(body: VBox.(BuildContext) -> Unit): VBox {
            val builder = VBox(this.ctx)
            addNode(builder, body)
            return builder
        }
    }
}

