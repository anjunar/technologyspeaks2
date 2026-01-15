package jFx2.controls

import jFx2.core.Component
import jFx2.core.Container
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.core.dsl.registerField
import jFx2.forms.ArrayFormContext
import jFx2.forms.Formular
import org.w3c.dom.HTMLFieldSetElement
import org.w3c.dom.Node

class ArrayForm(override val node: HTMLFieldSetElement, override val ui : UiScope)
    : Container<HTMLFieldSetElement>(node) {

    private val subForms = ArrayList<Formular>()

    fun registerInput(index : Int, input: Any) {
        subForms.add(index, input as Formular)
    }

    fun unregisterInput(index : Int) {
        subForms.removeAt(index)
    }

    fun inputOrNull(index: Int): Any? = subForms.getOrNull(index)

    fun registerField(name: Int, field: Any): () -> Unit {
        registerInput(name, field)

        return {
            unregisterInput(name)
        }
    }
}

fun NodeScope.arrayForm(
    name: String = "form",
    block: NodeScope.() -> Unit
): ArrayForm {
    val el = create<HTMLFieldSetElement>("fieldset")

    val form = ArrayForm(el, ui)
    attach(form)

    val childForms = ArrayFormContext(
        form
    )

    registerField(name, form)

    NodeScope(ui, el as Node, form, childForms).block()
    return form
}