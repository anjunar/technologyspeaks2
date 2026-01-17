package app.pages

import app.core.User
import jFx2.controls.button
import jFx2.controls.div
import jFx2.controls.text
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.subscribeBidirectional
import jFx2.core.rendering.condition
import jFx2.core.rendering.foreach
import jFx2.core.runtime.component
import jFx2.forms.SizeValidator
import jFx2.forms.arrayForm
import jFx2.forms.form
import jFx2.forms.input
import jFx2.forms.inputContainer
import jFx2.forms.subForm
import jFx2.modals.window
import jFx2.state.Property
import kotlinx.serialization.json.Json
import org.w3c.dom.HTMLDivElement

class Home(override var node: HTMLDivElement) : Component<HTMLDivElement>() {

    context(scope: NodeScope)
    fun afterBuild() {

        val count = Property(0)
        val showExtra = Property(true)

        val user =
            Json.decodeFromString<User>(" { \"nickName\": \"Anjunar\", \"userInfo\": { \"firstName\": \"Patrick\", \"lastName\": \"Bittner\" }, \"emails\": [{ \"value\" : \"anjunar@gmx.de\" }] } ")

        div {
            val myForm = form {
                condition(showExtra) {
                    then {
                        inputContainer("Nick name") {
                            input("nickName") {
                                validatorsProperty.add(SizeValidator(3, 12))
                                subscribeBidirectional(user.nickName, valueProperty)
                            }
                        }
                    }
                    elseDo {
                        div {
                            text { "count: ${count.get()}" }
                        }
                    }
                }

                subForm("userInfo") {
                    inputContainer("First name") {
                        input("firstName") {
                            validatorsProperty.add(SizeValidator(3, 12))
                            subscribeBidirectional(user.userInfo.firstName, valueProperty)
                        }
                    }
                    inputContainer("Last name") {
                        input("lastName") {
                            validatorsProperty.add(SizeValidator(3, 12))
                            subscribeBidirectional(user.userInfo.lastName, valueProperty)
                        }
                    }

                }

                arrayForm("emails") {
                    foreach(user.emails, { it.value }) { email, index ->
                        subForm(index = index.get()) {
                            inputContainer("Email") {
                                input("value") {
                                    validatorsProperty.add(SizeValidator(3, 12))
                                    subscribeBidirectional(email.value, valueProperty)
                                }
                            }
                        }
                    }
                }
            }

            button("Set") {
                onClick {
                    user.userInfo.firstName.set("Patrick")
                    console.log(user.toString())
                }
            }
            button("Toggle") {
                onClick {
                    showExtra.set(!showExtra.get())
                    console.log(myForm.fields.keys.joinToString(", ") { it })
                    console.log(myForm.subForms.keys.joinToString(", ") { it })
                }
            }
        }

    }
}

context(scope: NodeScope)
fun homePage(block: context(NodeScope) Home.() -> Unit = {}): Home {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("home-page")
    val c = Home(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

    scope.ui.build.afterBuild {
        with(childScope) {
            c.afterBuild()
        }
    }

    block(childScope, c)

    return c
}
