package com.anjunar.technologyspeaks

import com.anjunar.technologyspeaks.security.LinkBuilder
import com.anjunar.technologyspeaks.core.UsersController
import com.anjunar.technologyspeaks.security.IdentityHolder
import com.anjunar.technologyspeaks.security.LoginController
import com.anjunar.technologyspeaks.security.RegisterController
import jakarta.annotation.security.RolesAllowed
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ApplicationController(val identityHolder: IdentityHolder) {

    @GetMapping(value = [""], produces = ["application/json"])
    @RolesAllowed("Anonymous", "Guest",  "User", "Administrator")
    fun main() : Application {

        val application = Application(identityHolder.user)

        application.addLinks(
            LinkBuilder.create(LoginController::options)
                .withRel("login")
                .build(),
            LinkBuilder.create(RegisterController::options)
                .withRel("register")
                .build(),
            LinkBuilder.create(UsersController::list)
                .withRel("users")
                .build()
        )

        return application
    }

}