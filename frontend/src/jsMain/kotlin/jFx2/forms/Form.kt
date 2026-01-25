package jFx2.forms

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabitities.FormContextKey
import jFx2.core.capabitities.FormOwnerKey
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.registerSubForm
import org.w3c.dom.HTMLFormElement

class Form(override val node: HTMLFormElement) : Component<HTMLFormElement>(), Formular {

    val fields: MutableMap<String, FormField<*, *>> = LinkedHashMap()
    val subForms: MutableMap<String, Form> = LinkedHashMap()

    var submitHandler: (() -> Unit)? = null

    fun onSubmit(handler: () -> Unit) { submitHandler = handler }

    fun initialize() {
        node.addEventListener("submit", { event -> event.preventDefault(); submitHandler?.invoke() })
    }

    internal fun registerField(name: String, field: FormField<*, *>) {
        fields[name] = field
    }

    internal fun unregisterField(name: String) { fields.remove(name) }

    internal fun registerSubForm(namespace: String, sub: Form) {
        subForms[namespace] = sub
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

    val childScope = scope.fork(
        parent = c.node,
        owner = c,
        ctx = scope.ctx.fork().also {
            it.set(FormOwnerKey, c)
        },
        ElementInsertPoint(c.node)
    )

    scope.ui.build.afterBuild { c.initialize() }

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

    val formContextParent = runCatching { scope.ctx.get(FormContextKey) }.getOrNull()
    val formContext = FormContext(formContextParent, if (index > -1) index.toString() else namespace)
    val childScope = scope.fork(
        parent = c.node,
        owner = c,
        ctx = scope.ctx.fork().also {
            it.set(FormContextKey, formContext)
            it.set(FormOwnerKey, c)
        },
        ElementInsertPoint(c.node)
    )

    block(childScope, c)

    if (index > -1) {
        registerSubForm(index, c)
    } else {
        registerSubForm(namespace, c)
    }


    return c
}
