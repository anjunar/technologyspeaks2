import controls.input
import controls.placeholder
import controls.value
import core.mountInto
import core.span
import core.text
import core.vbox
import kotlinx.browser.document
import state.Property
import state.map

fun main() {

    val text = Property("Hello")

    mountInto(document.body!!) {
        vbox {
            span {
                text(text.map { it })
            }

            input {
                placeholder = "Type..."
                value(text)
            }
        }
    }
}
