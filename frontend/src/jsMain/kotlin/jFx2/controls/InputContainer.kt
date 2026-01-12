package jFx2.controls

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import org.w3c.dom.HTMLDivElement

class InputContainer(
    override val node: HTMLDivElement
) : Component<HTMLDivElement> {

    internal var ownerScope: NodeScope? = null

    var input: Input? = null
        private set

    var placeholder: String = ""
        set(v) {
            field = v
            input?.placeholder = v
        }

    fun input(block: Input.() -> Unit = {}): Input {
        val scope = ownerScope
            ?: error("InputContainer.input() can only be used inside inputContainer { ... }")

        val childScope = NodeScope(scope.ui, node)
        val i = childScope.input(input?.name!!, block)

        input = i

        if (placeholder.isNotBlank()) {
            i.placeholder = placeholder
        }

        return i
    }

    internal fun applyToChildren() {
        input?.placeholder = placeholder
    }
}

fun NodeScope.inputContainer(block: InputContainer.() -> Unit): InputContainer {
    val el = create<HTMLDivElement>("div")
    el.classList.add("input-container")

    val c = InputContainer(el)
    attach(c)

    val prev = c.ownerScope
    c.ownerScope = this
    try {
        c.block()
    } finally {
        c.ownerScope = prev
    }

    c.applyToChildren()
    return c
}
