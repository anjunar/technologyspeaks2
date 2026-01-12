package jFx2.controls

import jFx2.core.capabilities.DisposeScope
import jFx2.core.capabilities.DomScope
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.events.Event

context(scope: DomScope, disposeScope: DisposeScope)
fun button(
    text: String,
    onClick: (Event) -> Unit
): HTMLButtonElement {
    val el = scope.create<HTMLButtonElement>("button")
    el.textContent = text

    val listener: (Event) -> Unit = { e -> onClick(e) }
    el.addEventListener("click", listener)

    disposeScope.register { el.removeEventListener("click", listener) }
    return el
}