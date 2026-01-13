package app

import jFx2.controls.SizeValidator
import jFx2.controls.button
import jFx2.controls.div
import jFx2.controls.form
import jFx2.controls.input
import jFx2.controls.inputContainer
import jFx2.controls.subForm
import jFx2.core.dsl.text
import jFx2.core.rendering.condition
import jFx2.core.runtime.component
import jFx2.forms.FormRegistryScope
import jFx2.state.Property
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement

fun main() {
    val root = document.createElement("div") as HTMLDivElement

    val count = Property(0)
    val showExtra = Property(true)

    component(root) {
        div {
            form {
                condition(showExtra) {
                    then {
                        inputContainer("Nick name") {
                            field {
                                input("nickName") {
                                    validators.add(SizeValidator(3, 12))
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
                                validators.add(SizeValidator(3, 12))
                            }
                        }
                    }
                    inputContainer("Last name") {
                        field {
                            input("lastName") {
                                validators.add(SizeValidator(3, 12))
                            }
                        }
                    }

                }

            }

            button("Count +1") {
                onClick { count.set(count.get() + 1) }
            }
            button("Toggle extra") {
                onClick {
                    showExtra.set(!showExtra.get())
                    val reg = formRegistry
                    console.log(reg?.resolveOrNull("form.nickName"))
                    console.log(reg?.resolveOrNull("form.userInfo.firstName"))
                }
            }

        }
    }

    document.body!!.appendChild(root)
}
