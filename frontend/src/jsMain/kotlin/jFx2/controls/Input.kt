package jFx2.controls

import jFx2.core.capabilities.NodeScope
import jFx2.core.dsl.registerField
import jFx2.forms.FormField
import org.w3c.dom.HTMLInputElement

class Input(val name : String, override val node: HTMLInputElement) : FormField<String, HTMLInputElement>, HasPlaceholder {

    override var placeholder: String
        get() = node.placeholder
        set(v) {
            node.placeholder = v
        }

    fun value(): String = node.value
    fun setValue(v: String) {
        node.value = v
    }

    override fun read(): String {
        return node.value
    }
}

fun NodeScope.input(name : String, block: Input.() -> Unit = {}): Input {
    val el = create<HTMLInputElement>("input")
    val c = Input(name, el)
    c.block()
    registerField(name, c)
    attach(c)
    return c
}