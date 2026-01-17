package jFx2.controls

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.state.Disposable
import jFx2.state.Property
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.events.MouseEvent

class Button(override val node: HTMLButtonElement, val ui : UiScope) : Component<HTMLButtonElement>() {

    fun onClick(handler: (MouseEvent) -> Unit) {
        val h: (dynamic) -> Unit = { e ->
            handler(e.unsafeCast<MouseEvent>())
            ui.build.flush()
        }
        node.addEventListener("click", h)
        onDispose { node.removeEventListener("click", h) }
    }

    fun disabled(value: Boolean) {
        node.disabled = value
    }

    fun disabled(flag: Property<Boolean>) {
        onDispose(flag.observe { node.disabled = it })
    }

    fun type(value: String) {
        node.type = value
    }

    fun text(value: String) {
        node.textContent = value
    }

    fun text(value: () -> String) {
        node.textContent = value()
    }
}

context(scope: NodeScope)
fun button(
    name : String,
    block: context(NodeScope) Button.() -> Unit = {}
): Button {
    val el = scope.create<HTMLButtonElement>("button")
    el.textContent = name
    val c = Button(el, scope.ui)

    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))
    block(childScope, c)

    return c
}
