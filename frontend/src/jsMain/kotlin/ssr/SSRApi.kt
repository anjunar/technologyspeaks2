@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package ssr

import jFx2.state.JobRegistry
import kotlinx.browser.window
import kotlinx.coroutines.promise

@JsExport
object SSRApi {

    // Eine zentrale Registry, die auch die UI/DSL benutzt!
    val jobs = JobRegistry.instance

    fun renderToString() = jobs.scope.promise {
        // 1) SSR Setup: route setzen, root erstellen, component mounten etc.
        // (hier nur placeholder)
        // window.location.href = url  // je nach SSR env / happy-dom

        // 2) initial render triggern (muss Jobs über SSRApi.jobs starten!)
        // renderApp(url)

        // 3) warten bis ALLE Jobs fertig sind (inkl. late jobs)
        jobs.awaitIdle(timeoutMs = 10_000)

        // 4) final flush (falls du sowas hast)
        // ui.build.flush()

        // 5) HTML String holen
        window.document.documentElement?.outerHTML ?: ""
    }
}
