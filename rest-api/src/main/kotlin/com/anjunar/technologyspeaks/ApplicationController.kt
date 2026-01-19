package com.anjunar.technologyspeaks

import com.anjunar.technologyspeaks.security.LinkBuilder
import com.anjunar.technologyspeaks.core.UsersController
import com.anjunar.technologyspeaks.security.IdentityHolder
import com.anjunar.technologyspeaks.security.LogoutController
import com.anjunar.technologyspeaks.security.PasswordLoginController
import com.anjunar.technologyspeaks.security.PasswordRegisterController
import com.anjunar.technologyspeaks.security.WebAuthnLoginController
import com.anjunar.technologyspeaks.security.WebAuthnRegisterController
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
            LinkBuilder.create(WebAuthnLoginController::options)
                .withRel("login")
                .build(),
            LinkBuilder.create(WebAuthnRegisterController::options)
                .withRel("register")
                .build(),
            LinkBuilder.create(PasswordLoginController::login)
                .withRel("login")
                .build(),
            LinkBuilder.create(PasswordRegisterController::register)
                .withRel("register")
                .build(),
            LinkBuilder.create(LogoutController::logout)
                .withRel("logout")
                .build(),
            LinkBuilder.create(UsersController::list)
                .withRel("users")
                .build()
        )

        return application
    }

}