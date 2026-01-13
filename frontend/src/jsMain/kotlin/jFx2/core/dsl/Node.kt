package jFx2.core.dsl

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.css.CSSStyleDeclaration

fun NodeScope.text(value: () -> String) {

    build.dirty {
        val result = value()
        val tn = ui.dom.textNode(result)
        ui.dom.attach(parent, tn)
    }

}

fun NodeScope.style(block: CSSStyleDeclaration.() -> Unit) {
    val el = (parent as? HTMLElement)
        ?: error("style() can only be used on HTMLElement nodes, but was: ${parent::class.simpleName}")

    build.dirty {
        el.style.block()
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

fun NodeScope.render(child: Component<out Node>) {
    ui.attach(parent, child)
}