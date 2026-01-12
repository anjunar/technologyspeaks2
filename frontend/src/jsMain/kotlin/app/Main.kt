@file:OptIn(ExperimentalWasmJsInterop::class, ExperimentalSerializationApi::class)

package app

import jFx.controls.Button.Companion.button
import jFx.controls.Form
import jFx.controls.Form.Companion.form
import jFx.controls.Input
import jFx.controls.Input.Companion.input
import jFx.controls.InputContainer.Companion.inputContainer
import jFx.controls.SubForm.Companion.subForm
import jFx.core.DSL.component
import jFx.core.DSL.condition
import jFx.layout.Div
import jFx.layout.Div.Companion.div
import jFx.state.Property
import kotlinx.browser.document
import kotlinx.serialization.ExperimentalSerializationApi
import org.w3c.dom.HTMLElement

fun main() {

    fun counterComponent(): HTMLElement {

        var formular: Form? = null

        val showStreet = Property(false)

        val div: Div = component {
            div {
                formular = form {
                    name = "user"
                    inputContainer {
                        placeholder = "Nickname"
                        input {
                            name = "nickName"
                            validators(Input.Companion.SizeValidator(0, 12))
                        }
                    }

                    subForm {
                        name = "userInfo"
                        inputContainer {
                            placeholder = "First Name"
                            input {
                                name = "firstName"
                                validators(Input.Companion.SizeValidator(0, 12))
                            }
                        }
                        inputContainer {
                            placeholder = "Last Name"
                            input {
                                name = "lastName"
                                validators(Input.Companion.SizeValidator(0, 12))
                            }
                        }
                    }

                    button {
                        text = "Toggle Address"
                        onClick {
                            it.preventDefault()
                            showStreet.set(!showStreet.get()!!)
                        }
                    }

                    subForm {
                        name = "address"
                        condition(showStreet) {
                            inputContainer {
                                placeholder = "Street"
                                input {
                                    name = "street"
                                    validators(Input.Companion.SizeValidator(0, 80))
                                }
                            }
                        }
                    }

                    button {
                        text = "Submit"
                        onClick {
                            it.preventDefault()
                            println(formular.toString())
                        }
                    }

                }

            }
        }

        return div.build()
    }

    val root = document.getElementById("root")!!

    val renderCounter = counterComponent()

    root.appendChild(renderCounter)

}
