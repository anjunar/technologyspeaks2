package jFx.layout

import jFx.core.DSL.ParentScope

class HBox : AbstractBox("hbox") {
    companion object {
        fun ParentScope.vbox(body: HBox.() -> Unit): HBox {
            val builder = HBox()
            addNode(builder, body)
            return builder
        }
    }
}

