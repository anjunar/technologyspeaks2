package jFx2.controls

import jFx2.core.Component
import jFx2.core.Container
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.forms.Formular
import jFx2.forms.NamedFormContext
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.Node

class Form(override val node: HTMLFormElement, override val ui : UiScope)
    : Container<HTMLFormElement>(node), Formular {

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

fun NodeScope.form(
    block: NodeScope.() -> Unit
): Form {
    val el = create<HTMLFormElement>("form")

    val form = Form(el, ui)
    attach(form)

    val childForms = NamedFormContext(form)

    NodeScope(ui, el as Node, form, childForms).block()
    return form
}