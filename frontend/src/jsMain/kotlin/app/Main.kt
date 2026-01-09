package app

import jFx.controls.Button.Companion.button
import jFx.controls.Input.Companion.input
import jFx.controls.InputContainer.Companion.inputContainer
import jFx.core.DSL.component
import jFx.layout.Div
import jFx.layout.Div.Companion.div
import jFx.state.Property
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import kotlin.random.Random

fun main() {

    fun counterComponent(): HTMLElement {
        val count = Property("tst")

        val div : Div = component {
            div {
                button {
                    text = "Click me"
                    textReader { count.get()!! }
//                    textProperty.subscribe(count)
                    onClick {
                        count.set(count.get() + "+")
                    }
                }

                inputContainer {
                    placeholder = "Enter text"
                    input {
                        valueWriter { count.set(it + "aa")}
                        placeholder = "Enter text"
                    }
                }
            }
        }

        return div.build()
    }

    val root = document.getElementById("root")!! // <div id="root"></div>

    val renderCounter = counterComponent()

    root.appendChild(renderCounter)

}
