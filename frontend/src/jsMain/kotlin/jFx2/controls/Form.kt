package jFx2.controls

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.forms.FormRegistryScope
import jFx2.forms.FormScope
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.Node

class Form(
    override val node: HTMLFormElement,
    val formScope: FormScope,
    val registry: FormRegistryScope?
) : Component<HTMLFormElement> {

    private val inputsByName = LinkedHashMap<String, Any>()

    fun registerInput(name: String, input: Any) {
        inputsByName[name] = input
    }

    fun inputOrNull(name: String): Any? = inputsByName[name]
}

fun NodeScope.form(
    name: String = "form",
    registry: FormRegistryScope? = this.formRegistry,
    block: NodeScope.() -> Unit
): Form {

    val el = create<HTMLFormElement>("form")

    val formScope = FormScope(name)
    val form = Form(el, formScope, registry)

    attach(form)

    val childUi = UiScope(
        dom = ui.dom,
        build = ui.build,
        render = ui.render,
        dispose = ui.dispose,
        formScope = formScope,
        formRegistry = registry
    )

    NodeScope(childUi, el as Node).block()


    return form
}