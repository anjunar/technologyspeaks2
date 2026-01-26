package app

import app.services.ApplicationService
import jFx2.controls.link
import jFx2.controls.text
import jFx2.core.dsl.className
import jFx2.core.rendering.dynamicOutlet
import jFx2.core.rendering.foreach
import jFx2.core.rendering.observeRender
import jFx2.core.runtime.component
import jFx2.forms.editor
import jFx2.forms.form
import jFx2.layout.div
import jFx2.layout.hbox
import jFx2.layout.vbox
import jFx2.modals.viewport
import jFx2.router.router
import jFx2.router.windowRouter
import jFx2.state.JobRegistry
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLDivElement

fun main() {
    val root = document.getElementById("root") as HTMLDivElement

    val jobs = JobRegistry.instance

    jobs.launch(label = "ApplicationService.invoke", owner = "app") {
        ApplicationService.invoke()
    }

    component(root) {
        vbox {

            hbox {
                className { "app-header-bar" }
            }

            div {
                className { "app-shell-body" }

                observeRender(ApplicationService.app) { app ->
                    div {
                        className { "glass app-shell-nav" }

                        foreach(app.links, { key -> key.id }) { link, index ->
                            link(link.url) {
                                text { link.name }
                            }
                        }
                    }
                }

                viewport {
                    windowRouter(Routes.routes) { }
                }
            }


            hbox {
                className { "app-footer-bar" }

                foreach(jobs.entries, { key -> key.id }) { job, index ->
                    div {
                        text {
                            job.label
                        }
                    }
                }
            }
        }

    }

}
