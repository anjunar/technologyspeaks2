package app

import app.services.ApplicationService
import jFx2.controls.link
import jFx2.controls.text
import jFx2.core.dsl.className
import jFx2.core.dsl.style
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
        component(root) {
            vbox {

                hbox {
                    style {
                        alignItems = "space-between"
                        backgroundColor = "var(--color-background-secondary)"
                        height = "32px"
                    }
                }

                div {
                    style {
                        flex = "1"
                        alignItems = "center"
                    }

                    vbox {
                        className { "glass" }
                        style {
                            position = "absolute"
                            left = "16px"
                            top = "50%"
                            transform = "translateY(-50%)"
                            padding = "16px"
                            height = "unset"
                        }

                        foreach(ApplicationService.app.links, { key -> key.id }) { link, index ->
                            link(link.url) {
                                text { link.name }
                            }
                        }

                    }

                    windowRouter(Routes.routes) { }
                }



                hbox {
                    style {
                        alignItems = "space-between"
                        backgroundColor = "var(--color-background-secondary)"
                        height = "32px"
                    }
                }
            }

        }
    }
}
