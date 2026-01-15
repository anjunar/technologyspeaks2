package jFx2.core

import jFx2.core.capabilities.Disposable
import jFx2.state.ListProperty
import org.w3c.dom.Node

abstract class Component<N : Node>() {
    abstract val node: N
    fun dispose() {}

    val classProperty = ListProperty<String>()
    var classBinding: Disposable? = null
}