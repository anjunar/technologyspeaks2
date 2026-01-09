package app

import jFx.controls.Button.Companion.button
import jFx.controls.Input.Companion.input
import jFx.controls.InputContainer.Companion.inputContainer
import jFx.core.DSL.component
import jFx.layout.Div.Companion.div
import jFx.state.Property
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

fun main() {

    fun counterComponent(): HTMLElement {
        val count = Property("tst")

        return component {
            div {
                button {
                    text = "+"
                    textProperty.subscribe(count)
                    onClick {
                        count.set(count.get() + "+")
                    }
                }

                inputContainer {
                    placeholder = "Count"

                    input {

                    }
                }
            }
        }
    }

    val root = document.getElementById("root")!! // <div id="root"></div>

    val renderCounter = counterComponent()

    root.appendChild(renderCounter)

}
