package jFx2.core

import jFx2.state.ListProperty

abstract class Component<N : org.w3c.dom.Node>() {
    abstract val node: N
    fun dispose() {}

    val classProperty = ListProperty<String>()
    var classBinding: jFx2.forms.Disposable? = null
}