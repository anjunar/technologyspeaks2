package controls

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.ElementCreationOptions
import org.w3c.dom.ElementDefinitionOptions
import org.w3c.dom.HTMLInputElement


object FxInput {
    abstract class Component() : HTMLInputElement() {

        fun connectedCallback() {
            placeholder = "Placeholder!!!"
        }

    }

    fun create(): Element {
        return document.createElement(  "input", ElementCreationOptions("fx-input"))
    }

    val element = window.customElements.define(
        "fx-input",
        Component::class.js.unsafeCast<dynamic>(),
        ElementDefinitionOptions(extends = "input")
    )
}

