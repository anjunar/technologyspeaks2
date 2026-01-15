package jFx2.forms

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabitities.FormOwnerKey
import jFx2.core.dsl.registerSubForm
import org.w3c.dom.HTMLFieldSetElement

class ArrayForm(override val node: HTMLFieldSetElement) : Component<HTMLFieldSetElement>(), Formular {

    val subForms: MutableList<Formular> = mutableListOf()

    internal fun registerSubForm(index : Int, form: Formular) {
        subForms.add(index, form)
    }

    internal fun unregisterSubForm(form: Formular) {
        subForms.remove(form)
    }

}

context(scope: NodeScope)
fun arrayForm(
    namespace: String,
    block: context(NodeScope) ArrayForm.() -> Unit
): ArrayForm {
    val el = scope.create<HTMLFieldSetElement>("fieldset")

    val c = ArrayForm(el)
    scope.attach(c)

    val childScope = NodeScope(
        ui = scope.ui,
        parent = c.node,
        owner = c,
        ctx = scope.ctx.fork().also {
            it.set(FormOwnerKey, c)
        },
        scope.dispose
    )

    block(childScope, c)

    registerSubForm(namespace, c)

    return c
}
