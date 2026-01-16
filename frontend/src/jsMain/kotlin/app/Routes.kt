package app

import app.pages.Home
import app.pages.Login
import jFx2.router.Route
import kotlinx.browser.window
import kotlinx.coroutines.await

object Routes {

    val routes = listOf<Route<*>>(
        Route(
            path = "/desktop",
            component = Home::class,
            resolve =  { home ->
                val response = window.fetch("service").await()
                val result = response.json().await()
                home
            },
            children = listOf(
                Route(
                    path = "/login",
                    component = Login::class
                ),
                Route(
                    path = "/logout",
                    component = Login::class
                )
            )
        )
    )

}