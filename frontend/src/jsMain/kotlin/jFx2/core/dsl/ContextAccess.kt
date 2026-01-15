package jFx2.core.dsl

import jFx2.core.capabilities.NodeScope
import jFx2.core.capabitities.FormOwnerKey
import jFx2.forms.ArrayForm
import jFx2.forms.Form
import jFx2.forms.FormField
import jFx2.forms.Formular

context(scope: NodeScope)
fun registerField(name: String, field: FormField<*, *>) {

    val formOwner = runCatching { scope.ctx.get(FormOwnerKey) }.getOrNull()
    (formOwner as Form).registerField(name, field)

    scope.dispose.register {
        formOwner.unregisterField(name)
    }

}

context(scope: NodeScope)
fun registerSubForm(name: String, form: Formular) {

    val formOwner = runCatching { scope.ctx.get(FormOwnerKey) }.getOrNull()
    (formOwner as Form).registerSubForm(name, form)

    scope.dispose.register {
        formOwner.unregisterSubForm(name)
    }

}

context(scope: NodeScope)
fun registerSubForm(index: Int, form: Formular) {

    val formOwner = runCatching { scope.ctx.get(FormOwnerKey) }.getOrNull()
    (formOwner as ArrayForm).registerSubForm(index, form)

    scope.dispose.register {
        formOwner.unregisterSubForm(form)
    }

}