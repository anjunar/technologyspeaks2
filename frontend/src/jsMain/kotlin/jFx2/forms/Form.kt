package jFx2.forms

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabitities.FormOwnerKey
import jFx2.core.dsl.registerSubForm
import org.w3c.dom.HTMLFormElement

class Form(override val node: HTMLFormElement) : Component<HTMLFormElement>(), Formular {

    val fields: MutableMap<String, FormField<*, *>> = LinkedHashMap()
    val subForms: MutableMap<String, Formular> = LinkedHashMap()

    internal fun registerField(name: String, field: FormField<*, *>) {
        fields[name] = field
        onDispose { fields.remove(name) }
    }

    internal fun unregisterField(name: String) { fields.remove(name) }

    internal fun registerSubForm(namespace: String, sub: Formular) {
        subForms[namespace] = sub
        onDispose { subForms.remove(namespace) }
    }

    internal fun unregisterSubForm(namespace: String) { subForms.remove(namespace) }
}

context(scope: NodeScope)
fun form(
    namespace: String? = null,
    block: context(NodeScope) Form.() -> Unit
): Form {
    val el = scope.create<HTMLFormElement>("form")

    val c = Form(el)
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
    return c
}

context(scope: NodeScope)
fun subForm(
    namespace: String = "",
    index : Int = -1,
    block: context(NodeScope) Form.() -> Unit
): Form {
    val el = scope.create<HTMLFormElement>("fieldset")

    val c = Form(el)
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

    if (index > -1) {
        registerSubForm(index, c)
    } else {
        registerSubForm(namespace, c)
    }


    return c
}
