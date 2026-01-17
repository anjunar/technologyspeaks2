package jFx2.controls

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import kotlinx.browser.window
import org.w3c.dom.CustomEvent
import org.w3c.dom.HTMLAnchorElement

class Link(override val node: HTMLAnchorElement) : Component<HTMLAnchorElement>()

context(scope: NodeScope)
fun link(href : String, block: context(NodeScope) Link.() -> Unit = {}): Link {
    val el = scope.create<HTMLAnchorElement>("a")
    el.href = href
    el.addEventListener("click", {
        it.preventDefault()
        window.history.pushState(null, "", href)
        window.dispatchEvent(CustomEvent("popstate"))
    })
    val c = Link(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx)

    block(childScope, c)

    return c
}
