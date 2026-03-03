package com.anjunar.technologyspeaks.security

import com.anjunar.technologyspeaks.core.Role
import jakarta.annotation.security.RolesAllowed
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class ConfirmController(val identityHolder: IdentityHolder) {

    @PostMapping("/security/confirm")
    @RolesAllowed("Guest")
    fun confirm(@RequestParam("code") code : String) {

        val userRole = Role.query("name" to "User")

        if (identityHolder.credential.code == code) {
            identityHolder.credential.roles.clear()
            identityHolder.credential.roles.add(userRole!!)
        } else {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Access denied"
            )
        }

    }

}