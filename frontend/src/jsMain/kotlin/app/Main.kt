package app

import javascriptFx.controls.Button
import javascriptFx.core.KotlinDSL
import javascriptFx.core.KotlinDSL.component
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLDivElement

fun main() {

    val root: HTMLDivElement = component { ctx ->

        Fx.div()(ctx, this) {

            Fx.input()(ctx, this) {
                placeholder = "Search…"
                value = ""
            }

            Fx.button()(ctx, this) {
                text = "OK"
                onClick {
                    window.alert("clicked")
                }
            }
        }
    }

    document.body!!.appendChild(root)
}
