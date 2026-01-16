package com.anjunar.technologyspeaks

import com.anjunar.technologyspeaks.core.Address
import com.anjunar.technologyspeaks.core.Role
import com.anjunar.technologyspeaks.core.User
import com.anjunar.technologyspeaks.core.UserInfo
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class StartUpRunner {

    @Transactional
    fun run(vararg args: String) {

        var anonymousRole = Role.query("name" to "Anonymous")
        var guestRole = Role.query("name" to "Guest")
        var userRole = Role.query("name" to "User")
        var administratorRole = Role.query("name" to "Administrator")

        if (anonymousRole == null) {
            anonymousRole = Role("Anonymous", "Anonymous User")
            anonymousRole.persist()
        }

        if (guestRole == null) {
            guestRole = Role("Guest", "Guest User")
            guestRole.persist()
        }

        if (userRole == null) {
            userRole = Role("User", "User User")
            userRole.persist()
        }

        if (administratorRole == null) {
            administratorRole = Role("Administrator", "Administrator User")
            administratorRole.persist()
        }

    }
}