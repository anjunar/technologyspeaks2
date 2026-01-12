package jFx2.controls

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.events.Event

class Button(
    override val node: HTMLButtonElement
) : Component<HTMLButtonElement> {

    var text: String
        get() = node.textContent ?: ""
        set(v) { node.textContent = v }

    internal var flush: (() -> Unit)? = null

    internal var registerDispose: (((() -> Unit)) -> Unit)? = null

    fun onClick(handler: (Event) -> Unit) {
        val listener: (Event) -> Unit = { e ->
            handler(e)
            flush?.invoke()
        }

        node.addEventListener("click", listener)

        registerDispose?.invoke {
            node.removeEventListener("click", listener)
        }
    }
}

fun NodeScope.button(
    text: String? = null,
    block: Button.() -> Unit = {}
): Button {

    val el = create<HTMLButtonElement>("button")
    val btn = Button(el)

    btn.flush = { build.flush() }
    btn.registerDispose = { action -> dispose.register(action) }

    if (text != null) btn.text = text

    btn.block()

    attach(btn)

    return btn
}