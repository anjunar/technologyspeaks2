package jFx2.controls

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.core.dsl.registerField
import jFx2.forms.FormRegistryScope
import jFx2.forms.FormScope
import jFx2.forms.FormScopeImpl
import jFx2.forms.FormsContext
import jFx2.forms.Formular
import jFx2.forms.NamespacedFormRegistry
import org.w3c.dom.HTMLFieldSetElement
import org.w3c.dom.Node

class ArrayForm(override val node: HTMLFieldSetElement, val formScope: FormScope, val registry: FormRegistryScope?)
    : Component<HTMLFieldSetElement>() {

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
    registry: FormRegistryScope? = forms?.effectiveRegistry,
    block: NodeScope.() -> Unit
): ArrayForm {
    val el = create<HTMLFieldSetElement>("fieldset")

    val rootRegistry = forms?.rootRegistry
        ?: error("form() requires a root FormRegistryScope in NodeScope.forms")

    val parentScope = forms.scope
    val formScope =
        parentScope?.child(name) ?: FormScopeImpl(path = name, rootRegistry)

    val effectiveRegistry = registry?.let { NamespacedFormRegistry(formScope.path, it) }

    val form = ArrayForm(el, formScope, effectiveRegistry)
    attach(form)

    val childForms = FormsContext(
        rootRegistry = rootRegistry,
        scope = formScope,
        effectiveRegistry = effectiveRegistry,
        form
    )

    registerField(formScope.path, form)

    NodeScope(ui, el as Node, form, childForms).block()
    return form
}