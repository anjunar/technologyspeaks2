package app

import app.domain.core.Data
import app.domain.core.User
import app.domain.documents.Document
import app.domain.timeline.Post
import app.pages.core.UserPage
import app.pages.core.usersPage
import app.pages.documents.documentPage
import app.pages.homePage
import app.pages.security.logoutPage
import app.pages.security.passwordLoginPage
import app.pages.security.passwordRegisterPage
import app.pages.security.webAuthnLoginPage
import app.pages.security.webAuthnRegisterPage
import app.pages.timeline.PostsPage
import app.pages.timeline.postEditPage
import app.pages.timeline.postViewPage
import jFx2.client.JsonClient
import jFx2.router.Route
import org.w3c.fetch.RequestInit

object Routes {

    val routes = listOf(
        Route(
            path = "/",
            factory = { homePage {} },
            children = listOf(
                Route(
                    path = "document",
                    children = listOf(
                        Route(
                            path = "documents",
                            children = listOf(
                                Route(
                                    path = "document",
                                    children = listOf(
                                        Route(
                                            path = "root",
                                            factory = {
                                                val document = JsonClient.invoke<Data<Document>>("/service/document/documents/document/root",
                                                    RequestInit("POST")
                                                )

                                                documentPage {
                                                    model(document)
                                                }
                                            }
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                Route(
                    path = "timeline",
                    children = listOf(
                        Route(
                            path = "posts",
                            factory = { PostsPage.page { } },
                            children = listOf(
                                Route(
                                    path = "post",
                                    factory = {
                                        postEditPage {}
                                    }
                                ),
                                Route(
                                    path = "post/:id",
                                    factory = { params ->
                                        val post =
                                            JsonClient.invoke<Data<Post>>("/service/timeline/posts/post/" + params["id"]!!)
                                        postEditPage {
                                            model(post)
                                        }
                                    }
                                ),
                                Route(
                                    path = "post/:id/view",
                                    factory = { params ->
                                        val post =
                                            JsonClient.invoke<Data<Post>>("/service/timeline/posts/post/" + params["id"]!!)
                                        postViewPage {
                                            model(post)
                                        }
                                    }
                                )
                            )
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
                        )
                    )
                )
            )
        )
    )

}
