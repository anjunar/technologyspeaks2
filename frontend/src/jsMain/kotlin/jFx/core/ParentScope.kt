package jFx.core

import org.w3c.dom.Node

interface ParentScope {
    val ctx: BuildContext

    fun <T : Node, B : ElementBuilder<T>> addNode(builder: B, body: B.(BuildContext) -> Unit): T
}


