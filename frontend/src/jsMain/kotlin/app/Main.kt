package app

import app.services.ApplicationService
import jFx2.controls.link
import jFx2.controls.text
import jFx2.core.dsl.className
import jFx2.core.rendering.dynamicOutlet
import jFx2.core.rendering.foreach
import jFx2.core.runtime.component
import jFx2.layout.div
import jFx2.layout.hbox
import jFx2.layout.vbox
import jFx2.router.router
import jFx2.router.windowRouter
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement

fun main() {
    val root = document.getElementById("root") as HTMLDivElement

    ApplicationService.invoke().invokeOnCompletion {
    }

    component(root) {
        vbox {

            hbox {
                className { "app-shell-bar" }
            }

            div {
                className { "app-shell-body" }

                div {
                    className { "glass app-shell-nav" }

                    ApplicationService.app.observe { app ->
                        if (app != null) {
                            foreach(app.links, { key -> key.id }) { link, index ->
                                link(link.url) {
                                    text { link.name }
                                }
                            }
                        }
                    }

                }

                windowRouter(Routes.routes) { }
            }



            hbox {
                className { "app-shell-bar" }
            }
        }

    }

}
