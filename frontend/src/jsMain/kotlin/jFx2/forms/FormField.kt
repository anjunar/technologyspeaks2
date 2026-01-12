package jFx2.forms

import jFx2.core.Component
import jFx2.core.capabilities.Disposable
import org.w3c.dom.Node

interface FormField<V, T : Node> : Component<T> {

    fun read(): V
    fun write(value: V) {}

    fun observeValue(listener: (V) -> Unit): Disposable = { }

}