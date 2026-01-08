import controls.FxInput
import controls.FxInputContainer
import dsl.*
import kotlinx.browser.document

fun main() {
    val root = document.getElementById("root")

    mountInto(document.body!!) {
        div {
            className("app")
            vbox {
                span { text("Hello111a12") }
                span { text("World") }
            }

        }
    }

    val inputContainer = FxInputContainer.create()

    val input = FxInput.create()

    inputContainer.append(input)

    root?.append(inputContainer)
}