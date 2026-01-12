package jFx.core

import org.w3c.dom.Node

interface NodeBuilder<C : Node> : ElementBuilder<C> {
    fun registerLayoutListener() {}
}

