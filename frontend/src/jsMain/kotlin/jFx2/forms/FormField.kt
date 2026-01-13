package jFx2.forms

import jFx2.controls.Status
import jFx2.core.Component
import jFx2.core.capabilities.Disposable
import jFx2.state.ListProperty
import org.w3c.dom.Node

abstract class FormField<V, T : Node> : Component<T>() {

    val statusProperty = ListProperty<String>()

    val errorsProperty = ListProperty<String>()

    abstract fun read(): V
    fun write(value: V) {}

    abstract fun observeValue(listener: (V) -> Unit): Disposable

}