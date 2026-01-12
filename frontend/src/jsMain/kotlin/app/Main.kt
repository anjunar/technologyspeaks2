package app

import jFx2.controls.button
import jFx2.controls.div
import jFx2.controls.form
import jFx2.controls.input
import jFx2.controls.inputContainer
import jFx2.core.dsl.text
import jFx2.core.rendering.condition
import jFx2.core.runtime.component
import jFx2.forms.FormRegistry
import jFx2.state.Property
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement

fun main() {
    val root = document.createElement("div") as HTMLDivElement

    val count = Property(0)
    val showExtra = Property(true)

    component(root) {
        div {
            val formular = form {
                condition(showExtra) {
                    then {
                        div {
                            inputContainer {
                                placeholder = "Extra input"
                                field {
                                    input("firstName") {
                                        placeholder = "Type..."
                                    }
                                }
                            }
                        }
                    }
                    elseDo {
                        div {
                            text {
                                "Extra input hidden"
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
                    val reg = formRegistry as? FormRegistry
                    console.log(reg?.values("form"))
                }
            }

        }
    }

    document.body!!.appendChild(root)
}
