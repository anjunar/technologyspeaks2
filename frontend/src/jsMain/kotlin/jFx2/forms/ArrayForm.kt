package jFx2.forms

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabitities.ArrayFormOwnerKey
import jFx2.core.capabitities.FormContextKey
import org.w3c.dom.HTMLFieldSetElement

class ArrayForm(override val node: HTMLFieldSetElement) : Component<HTMLFieldSetElement>(), Formular {

    val subForms: MutableList<Form> = mutableListOf()

    internal fun registerSubForm(index : Int, form: Form) {
        subForms.add(index, form)
    }

    internal fun unregisterSubForm(form: Form) {
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

    val formContextParent = runCatching { scope.ctx.get(FormContextKey) }.getOrNull()
    val formContext = FormContext(formContextParent, namespace)
    val childScope = scope.fork(
        parent = c.node,
        owner = c,
        ctx = scope.ctx.fork().also {
            it.set(FormContextKey, formContext)
            it.set(ArrayFormOwnerKey, c)
        }
    )

    block(childScope, c)

    return c
}
