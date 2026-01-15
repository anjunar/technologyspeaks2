package jFx2.core.dsl

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.forms.ArrayFormContext
import jFx2.forms.NamedFormContext
import org.w3c.dom.Node

inline fun <reified C : Component<*>> NodeScope.owner(): C =
    (owner as? C) ?: error("No owner of type ${C::class.simpleName} in this NodeScope")

fun NodeScope.registerField(name: String, field: Any) {
    val f = (forms as NamedFormContext).form
    val d = f.registerField(name, field)
    dispose.register(d)
}

fun NodeScope.registerField(index: Int, field: Any) {
    val f = (forms as ArrayFormContext).form

    val d = f.registerField(index, field)
    dispose.register(d)
}

fun NodeScope.render(child: Component<out Node>) {
    ui.attach(parent, child)
}