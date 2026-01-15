package jFx2.controls

import jFx2.core.Component
import jFx2.core.Container
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.core.dsl.registerField
import jFx2.forms.Formular
import jFx2.forms.NamedFormContext
import org.w3c.dom.HTMLFieldSetElement
import org.w3c.dom.Node

class SubForm(override val node: HTMLFieldSetElement, override val ui : UiScope)
    : Container<HTMLFieldSetElement>(node), Formular {

    private val inputsByName = LinkedHashMap<String, Any>()

    override fun registerInput(name: String, input: Any) {
        inputsByName[name] = input
    }

    override fun unregisterInput(name: String) {
        inputsByName.remove(name)
    }

    override fun inputOrNull(name: String): Any? = inputsByName[name]

    override fun registerField(name: String, field: Any): () -> Unit {
        registerInput(name, field)

        return {
            unregisterInput(name)
        }
    }
}

fun NodeScope.subForm(
    name: String = "form",
    index : Int = -1,
    block: NodeScope.() -> Unit
): SubForm {
    val el = create<HTMLFieldSetElement>("fieldset")

    val form = SubForm(el,ui)
    attach(form)

    val childForms = NamedFormContext(form)

    if (index >= 0) {
        registerField(index, form)
    } else {
        registerField(name, form)
    }


    NodeScope(ui, el as Node, form, childForms).block()
    return form
}