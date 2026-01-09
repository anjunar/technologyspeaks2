@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package ssr

import kotlinx.browser.window

@JsExport
object SSRApi {

    fun renderToString(url: String): String {
        // hier: dein SSR-Render, der DOM nutzen darf
        return window.location.href
    }

}