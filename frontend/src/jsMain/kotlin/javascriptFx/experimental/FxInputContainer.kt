package javascriptFx.experimental

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

object FxInputContainer {

    abstract class Component() : HTMLElement() {


    }

    fun create(): Element {
        return document.createElement("fx-input-container")
    }

    val element = window.customElements.define(
        "fx-input-container",
        Component::class.js.unsafeCast<dynamic>()
    )


}