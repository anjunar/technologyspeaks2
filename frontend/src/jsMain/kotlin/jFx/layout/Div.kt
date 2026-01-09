package jFx.layout

import jFx.core.DSL.ParentScope

class Div : AbstractBox("") {
    companion object {
        fun ParentScope.div(body: Div.() -> Unit): Div {
            val builder = Div()
            addNode(builder, body)
            return builder
        }
    }
}

