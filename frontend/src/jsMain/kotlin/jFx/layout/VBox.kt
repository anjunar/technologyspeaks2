package jFx.layout

import jFx.core.DSL.ParentScope

class VBox : AbstractBox("vbox") {

    companion object {
        fun ParentScope.vbox(body: VBox.() -> Unit): VBox {
            val builder = VBox()
            addNode(builder, body)
            return builder
        }
    }
}

