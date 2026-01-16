package com.anjunar.technologyspeaks.security

import com.anjunar.technologyspeaks.core.Credential
import com.anjunar.technologyspeaks.core.PasswordCredential
import com.anjunar.technologyspeaks.core.Role
import com.anjunar.technologyspeaks.core.User
import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@Component
@RequestScope
class IdentityHolder(val sessionHolder: SessionHolder, val entityManager: EntityManager) {

    lateinit var user: User

    lateinit var roles: List<String>

    fun isAuthenticated() : Boolean {
        return sessionHolder.user != null
    }

    fun hasRole(role : String) : Boolean {
        return roles.contains(role)
    }

    @PostConstruct
    fun postConstruct() {

        if (sessionHolder.user == null || sessionHolder.credentials == null) {

            user = User("Anonymous")

            roles = listOf("Anonymous")


        } else {
            user = User.find(sessionHolder.user!!)!!

            val credential = entityManager.find(Credential::class.java, sessionHolder.credentials!!)!!

            roles = credential.roles.map { it.name }
        }


    }

}