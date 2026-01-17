package jFx2.router

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope

data class Route<C : Component<*>>(
    val path: String,
    val factory: suspend context(NodeScope) () -> Component<*>,
    val children: List<Route<*>> = emptyList()
)