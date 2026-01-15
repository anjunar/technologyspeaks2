package jFx2.core.dsl

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.forms.FormField
import org.w3c.dom.HTMLElement
import org.w3c.dom.css.CSSStyleDeclaration

context(scope: NodeScope)
fun renderField(field: Component<*>) {
    if (field.node.parentNode !== scope.parent) {
        scope.parent.appendChild(field.node)
    }
    scope.ui.dispose.register { field.dispose() }}

context(scope: NodeScope)
fun className(value: () -> String) {
    (scope.parent as HTMLElement).className = value()
}

context(scope: NodeScope)
fun style(block: CSSStyleDeclaration.() -> Unit) {
    (scope.parent as HTMLElement).style.unsafeCast<CSSStyleDeclaration>().block()
}




