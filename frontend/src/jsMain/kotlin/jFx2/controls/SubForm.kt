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

class SubForm(override val node: HTMLFieldSetElement, val formScope: FormScope, val registry: FormRegistryScope?)
    : Component<HTMLFieldSetElement>(), Formular {

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
        // Namespacing is handled by the effective registry (NamespacedFormRegistry).
        // We rely on the returned Disposable for unregistration.
        val d = registry?.register(name, field) ?: {}

        return {
            unregisterInput(name)
            d()
        }
    }
}

fun NodeScope.subForm(
    name: String = "form",
    index : Int = -1,
    registry: FormRegistryScope? = forms?.effectiveRegistry,
    block: NodeScope.() -> Unit
): SubForm {
    val el = create<HTMLFieldSetElement>("fieldset")

    val rootRegistry = forms?.rootRegistry
        ?: error("form() requires a root FormRegistryScope in NodeScope.forms")

    val parentScope = forms.scope
    val formScope =
        parentScope?.child(name) ?: FormScopeImpl(path = name, rootRegistry)

    val effectiveRegistry = registry?.let { NamespacedFormRegistry(formScope.path, it) }

    val form = SubForm(el, formScope, effectiveRegistry)
    attach(form)

    val childForms = FormsContext(
        rootRegistry = rootRegistry,
        scope = formScope,
        effectiveRegistry = effectiveRegistry,
        form
    )

    if (index >= 0) {
        registerField(index, form)
    } else {
        registerField(formScope.path, form)
    }


    NodeScope(ui, el as Node, form, childForms).block()
    return form
}