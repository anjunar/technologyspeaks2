package jFx2.controls

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.state.Disposable
import jFx2.state.Property
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.events.MouseEvent

class Button(
    override val node: HTMLButtonElement
) : Component<HTMLButtonElement>() {

    fun onClick(handler: (MouseEvent) -> Unit) {
        val h: (dynamic) -> Unit = { e -> handler(e.unsafeCast<MouseEvent>()) }
        node.addEventListener("click", h)
        onDispose(Disposable { node.removeEventListener("click", h) })
    }

    fun disabled(value: Boolean) {
        node.disabled = value
    }

    fun disabled(flag: Property<Boolean>) {
        onDispose(flag.observe { node.disabled = it })
    }

    fun type(value: String) {
        node.type = value // "button" | "submit" | "reset"
    }

    fun text(value: String) {
        node.textContent = value
    }

    fun text(value: () -> String) {
        // minimal: set once, and refresh on build flush triggers elsewhere.
        // If you want true reactive text binding, we can add a small TextBinding helper.
        node.textContent = value()
    }
}

context(scope: NodeScope)
fun button(
    block: context(NodeScope) Button.() -> Unit = {}
): Button {
    val el = scope.create<HTMLButtonElement>("button")
    val c = Button(el)

    scope.attach(c)

    val childScope = NodeScope(ui = scope.ui, parent = c.node, owner = c, ctx = scope.ctx, scope.dispose)
    block(childScope, c)

    return c
}
