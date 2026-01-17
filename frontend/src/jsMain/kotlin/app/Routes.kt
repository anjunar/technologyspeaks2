package app

import app.pages.Home
import app.pages.Login
import app.pages.Logout
import app.pages.homePage
import app.pages.loginPage
import app.pages.logoutPage
import jFx2.router.Route

object Routes {

    val routes = listOf<Route<*>>(
        Route<Home>(
            path = "/",
            factory = { homePage {} },
            children = listOf(
                Route<Login>(
                    path = "/login",
                    factory = { loginPage {} }
                ),
                Route<Logout>(
                    path = "/logout",
                    factory = { logoutPage {} }
                )
            )
        )
    )

}