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
    if (field.node.parentNode !== scope.insertPoint.parent) {
        scope.insertPoint.insert(field.node)
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
    var disposed = false

    fun scheduleNextFlush() {
        if (disposed) return
        scope.ui.build.dirty {
            if (disposed) return@dirty
            (scope.parent as HTMLElement).style.unsafeCast<CSSStyleDeclaration>().block()
        }
        scope.ui.build.afterBuild {
            scheduleNextFlush()
        }
    }

    scheduleNextFlush()
    scope.dispose.register { disposed = true }
}

context(scope: NodeScope)
fun mousedown(block: (MouseEvent) -> Unit) {
    val listener = block as (Event) -> Unit
    scope.parent.addEventListener("mousedown", listener)
    scope.dispose.register { scope.parent.removeEventListener("mousedown", listener) }
}