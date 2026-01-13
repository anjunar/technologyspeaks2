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

fun NodeScope.registerField(name: String, field: Any) {
    val fs = ui.formScope ?: error("registerField() used outside of a form scope")

    val qName = fs.qualify(name)

    val unregister = ui.formRegistry?.register(qName, field)
    if (unregister != null) {
        ui.dispose.register(unregister)
    }
}