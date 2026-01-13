package jFx2.controls

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.forms.FormRegistryScope
import jFx2.forms.FormScope
import jFx2.forms.FormScopeImpl
import jFx2.forms.FormsContext
import jFx2.forms.NamespacedFormRegistry
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.Node

class Form(override val node: HTMLFormElement, val formScope: FormScope, val registry: FormRegistryScope?) : Component<HTMLFormElement>() {

    private val inputsByName = LinkedHashMap<String, Any>()

    fun registerInput(name: String, input: Any) {
        inputsByName[name] = input
    }

    fun inputOrNull(name: String): Any? = inputsByName[name]
}

fun NodeScope.form(
    name: String = "form",
    registry: FormRegistryScope? = forms?.effectiveRegistry,
    block: NodeScope.() -> Unit
): Form {
    val el = create<HTMLFormElement>("form")

    val rootRegistry = forms?.rootRegistry
        ?: error("form() requires a root FormRegistryScope in NodeScope.forms")

    val parentScope = forms.scope
    val formScope =
        parentScope?.child(name) ?: FormScopeImpl(path = name, rootRegistry)

    val effectiveRegistry = registry?.let { NamespacedFormRegistry(formScope.path, it) }

    val form = Form(el, formScope, effectiveRegistry)
    attach(form)

    val childForms = FormsContext(
        rootRegistry = rootRegistry,
        scope = formScope,
        effectiveRegistry = effectiveRegistry
    )

    NodeScope(ui, el as Node, form, childForms).block()
    return form
}