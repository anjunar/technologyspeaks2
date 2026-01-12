package jFx2.controls.fields

import jFx2.core.capabilities.Disposable
import jFx2.forms.FormField
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event

class HtmlInputField(override val node: HTMLInputElement) : FormField<String, HTMLInputElement> {
    override fun read(): String = node.value
    override fun write(value: String) { node.value = value }

    override fun observeValue(listener: (String) -> Unit): Disposable {
        val l: (Event) -> Unit = { listener(node.value) }
        node.addEventListener("input", l)
        return { node.removeEventListener("input", l) }
    }
}