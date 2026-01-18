package app

import app.pages.Home
import app.pages.security.LoginPage
import app.pages.security.RegisterPage
import app.pages.homePage
import app.pages.security.loginPage
import app.pages.security.registerPage
import jFx2.router.Route

object Routes {

    val routes = listOf<Route<*>>(
        Route<Home>(
            path = "/",
            factory = { homePage {} },
            children = listOf(
                Route<LoginPage>(
                    path = "/security/login/options",
                    factory = { loginPage {} }
                ),
                Route<RegisterPage>(
                    path = "/security/register/options",
                    factory = { registerPage {} }
                )
            )
        )
    )

}