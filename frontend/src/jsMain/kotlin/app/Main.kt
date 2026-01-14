package app

import app.core.User
import jFx2.controls.SizeValidator
import jFx2.controls.button
import jFx2.controls.div
import jFx2.controls.form
import jFx2.controls.input
import jFx2.controls.inputContainer
import jFx2.controls.subForm
import jFx2.core.dsl.subscribeBidirectional
import jFx2.core.dsl.text
import jFx2.core.rendering.condition
import jFx2.core.rendering.foreach
import jFx2.core.runtime.component
import jFx2.state.Property
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import kotlinx.serialization.json.Json

fun main() {
    val root = document.createElement("div") as HTMLDivElement

    val count = Property(0)
    val showExtra = Property(true)

    val user = Json.decodeFromString<User>(" { \"nickName\": \"Anjunar\", \"userInfo\": { \"firstName\": \"Patrick\", \"lastName\": \"Bittner\" }, \"emails\": [{ \"value\" : \"anjunar@gmx.de\" }] } ")

    component(root) {
        div {
            val myForm = form {
                condition(showExtra) {
                    then {
                        inputContainer("Nick name") {
                            field {
                                input("nickName") {
                                    validatorsProperty.add(SizeValidator(3, 12))
                                    subscribeBidirectional(user.nickName, valueProperty)
                                }
                            }
                        }
                    }
                    elseDo {
                        div {
                            text {"count: ${count.get()}"}
                        }
                    }
                }

                subForm("userInfo") {
                    inputContainer("First name") {
                        field {
                            input("firstName") {
                                validatorsProperty.add(SizeValidator(3, 12))
                                subscribeBidirectional(user.userInfo.firstName, valueProperty)
                            }
                        }
                    }
                    inputContainer("Last name") {
                        field {
                            input("lastName") {
                                validatorsProperty.add(SizeValidator(3, 12))
                                subscribeBidirectional(user.userInfo.lastName, valueProperty)
                            }
                        }
                    }

                }

                subForm("emails") {
                    foreach(user.emails, { it.value }) { email, index ->
                        subForm("[$index]") {
                            inputContainer("Email") {
                                field {
                                    input("value") {
                                        validatorsProperty.add(SizeValidator(3, 12))
                                        subscribeBidirectional(email.value, valueProperty)
                                    }
                                }
                            }
                        }
                    }
                }

            }

            button("Console") {
                onClick {
                    user.userInfo.firstName.set("Patrick")
                    console.log(user.toString())
                }
            }
            button("Toggle extra") {
                onClick {
                    showExtra.set(!showExtra.get())
                    val reg = myForm.formScope
                    console.log(reg?.resolveOrNull("nickName"))
                    console.log(reg?.child("userInfo")?.resolveOrNull("firstName"))
                    console.log(reg?.child("emails")?.child(0)?.resolveOrNull("value"))
                }
            }

        }
    }

    document.body!!.appendChild(root)
}
