package jFx2.controls

import jFx2.core.capabilities.DomScope
import org.w3c.dom.HTMLDivElement

context(scope: DomScope)
fun div(body: HTMLDivElement.() -> Unit): HTMLDivElement {
    val el = scope.create<HTMLDivElement>("div")
    el.body()
    return el
}