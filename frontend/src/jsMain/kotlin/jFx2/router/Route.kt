package jFx2.router

import jFx2.core.Component
import kotlin.js.Promise
import kotlin.reflect.KClass

data class Route<C : Component<*>>(
    val path: String,
    val component: KClass<out C>? = null,
    val children: List<Route<*>> = emptyList(),
    val resolve : suspend (C) -> C? = { it }
)