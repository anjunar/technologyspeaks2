package com.anjunar.technologyspeaks.security

import jakarta.annotation.security.RolesAllowed
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.HandlerInterceptor

@Component
class SecurityInterceptor(val identityHolder: IdentityHolder) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        return when (handler) {
            is HandlerMethod -> {
                val rolesAllowed = handler.getMethodAnnotation(RolesAllowed::class.java) ?: return true

                if (! rolesAllowed.value.any { identityHolder.hasRole(it) }) {
                    throw ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Access denied"
                    )
                } else {
                    true
                }
            }
            else -> throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Access denied"
            )
        }

    }
}