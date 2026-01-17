package app

import jFx2.controls.link
import jFx2.controls.text
import jFx2.core.runtime.component
import jFx2.router.router
import jFx2.router.windowRouter
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement

fun main() {
    val root = document.createElement("div") as HTMLDivElement


    component(root) {

        link("/") {
            text("Home")
        }
        link("/login") {
            text("Login")
        }
        link("/logout") {
            text("Logout")
        }

        windowRouter(Routes.routes) {

        }
    }

    document.body!!.appendChild(root)
}
