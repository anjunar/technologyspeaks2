package jFx2.controls

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.forms.FormRegistryScope
import jFx2.forms.FormScope
import jFx2.forms.NamespacedFormRegistry
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.Node

class SubForm(override val node: HTMLFormElement, val formScope: FormScope, val registry: FormRegistryScope?) : Component<HTMLFormElement>() {

    private val inputsByName = LinkedHashMap<String, Any>()

    fun registerInput(name: String, input: Any) {
        inputsByName[name] = input
    }

    fun inputOrNull(name: String): Any? = inputsByName[name]
}

fun NodeScope.subForm(
    name: String = "form",
    registry: FormRegistryScope? = this.formRegistry,
    block: NodeScope.() -> Unit
): SubForm {

    val el = create<HTMLFormElement>("fieldset")

    val parentFormScope = ui.formScope
    val formScope = FormScope(name, parent = parentFormScope)

    val effectiveRegistry = when (registry) {
        null -> null
        else -> NamespacedFormRegistry(basePath = formScope.path, delegate = registry)
    }

    val form = SubForm(el, formScope, effectiveRegistry)
    attach(form)

    val childUi = UiScope(
        dom = ui.dom,
        build = ui.build,
        render = ui.render,
        dispose = ui.dispose,
        formScope = formScope,
        formRegistry = effectiveRegistry
    )

    NodeScope(childUi, el as Node, form).block()
    return form
}
