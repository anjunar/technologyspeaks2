package app

import app.domain.core.Data
import app.domain.core.User
import app.pages.Home
import app.pages.core.UserPage
import app.pages.core.usersPage
import app.pages.core.virtualListDemoPage
import app.pages.homePage
import app.pages.security.WebAuthnLoginPage
import app.pages.security.WebAuthnRegisterPage
import app.pages.security.logoutPage
import app.pages.security.passwordLoginPage
import app.pages.security.passwordRegisterPage
import app.pages.security.webAuthnLoginPage
import app.pages.security.webAuthnRegisterPage
import app.pages.timeline.PostsPage
import app.pages.timeline.postPage
import jFx2.client.JsonClient
import jFx2.router.Route

object Routes {

    val routes = listOf(
        Route(
            path = "/",
            factory = { homePage {} },
            children = listOf(
                Route(
                    path = "timeline",
                    children = listOf(
                        Route(
                            path = "post",
                            factory = { postPage { } }
                        ),
                        Route(
                            path = "posts",
                            factory = { PostsPage.page { } }
                        )
                    )
                ),
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
                            factory = { usersPage {} },
                            children = listOf(
                                Route(
                                    path = "user/:id",
                                    factory = { params ->
                                        val user =
                                            JsonClient.invoke<Data<User>>("/service/core/users/user/" + params["id"]!!)
                                        UserPage.page {
                                            model(user)
                                        }
                                    }
                                )
                            )
                        ),
                        Route(
                            path = "virtual-list",
                            factory = { virtualListDemoPage {} }
                        )
                    )
                )
            )
        )
    )

}
