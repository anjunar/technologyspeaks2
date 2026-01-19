package com.anjunar.technologyspeaks.security

import jakarta.annotation.security.RolesAllowed
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class LogoutController(val sessionHolder: SessionHolder) {

    @PostMapping("/security/logout")
    @RolesAllowed("Guest", "User", "Administrator")
    fun logout() {

        sessionHolder.invalidate()

    }

}