package jFx2.core.dsl

import jFx2.core.Component
import jFx2.core.capabilities.HasUi
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.forms.FormField
import org.w3c.dom.Node

fun NodeScope.text(value: () -> String) {

    build.dirty {
        val result = value()
        val tn = ui.dom.textNode(result)
        ui.dom.attach(parent, tn)
    }

}

fun <T> NodeScope.registerField(name: String, field: FormField<T, *>) {
    val fs = formScope ?: return
    val reg = formRegistry ?: return

    reg.registerField(fs, name, field)
    dispose.register { reg.unregisterField(fs, name, field) }
}