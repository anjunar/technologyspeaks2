package jFx2.core.dsl

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.forms.FormField
import org.w3c.dom.HTMLElement
import org.w3c.dom.css.CSSStyleDeclaration
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

context(scope: NodeScope)
fun renderField(field: Component<*>) {
    if (field.node.parentNode !== scope.parent) {
        scope.parent.appendChild(field.node)
    }
    scope.dispose.register {
        runCatching { field.dispose() }
        field.node.parentNode?.removeChild(field.node)
    }
}

context(scope: NodeScope)
fun renderFields(vararg field: Component<*>) {
    for (f in field) renderField(f)
}


context(scope: NodeScope)
fun className(value: () -> String) {
    (scope.parent as HTMLElement).className = value()
}

context(scope: NodeScope)
fun style(block: CSSStyleDeclaration.() -> Unit) {
    scope.ui.build.dirty{
        (scope.parent as HTMLElement).style.unsafeCast<CSSStyleDeclaration>().block()
    }
}

context(scope: NodeScope)
fun mousedown(block: (MouseEvent) -> Unit) {
    val listener = block as (Event) -> Unit
    scope.parent.addEventListener("mousedown", listener)
    scope.dispose.register { scope.parent.removeEventListener("mousedown", listener) }
}