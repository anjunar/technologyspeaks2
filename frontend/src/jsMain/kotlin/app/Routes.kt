package app

import app.pages.Home
import app.pages.core.usersPage
import app.pages.homePage
import app.pages.security.WebAuthnLoginPage
import app.pages.security.WebAuthnRegisterPage
import app.pages.security.logoutPage
import app.pages.security.passwordLoginPage
import app.pages.security.passwordRegisterPage
import app.pages.security.webAuthnLoginPage
import app.pages.security.webAuthnRegisterPage
import jFx2.router.Route

object Routes {

    val routes = listOf(
        Route(
            path = "/",
            factory = { homePage {} },
            children = listOf(
                Route(
                    path = "security",
                    children = listOf(
                        Route(path = "login", factory = { passwordLoginPage {} }),
                        Route(path = "register", factory = { passwordRegisterPage {} }),
                        Route(path = "login/options", factory = { webAuthnLoginPage {} }),
                        Route(path = "register/options", factory = { webAuthnRegisterPage {} }),
                        Route(path = "logout", factory = { logoutPage {} })
                    )
                ),
                Route(
                    path = "core",
                    children = listOf(
                        Route(
                            path = "users",
                            factory = { usersPage {} }
                        )
                    )
                )
            )
        )
    )

}