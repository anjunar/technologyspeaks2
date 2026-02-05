package com.anjunar.technologyspeaks.security

import jakarta.annotation.security.RolesAllowed
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class SecurityInterceptor(val identityHolder: IdentityHolder) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        return when (handler) {
            is HandlerMethod -> {
                val rolesAllowed = handler.getMethodAnnotation(RolesAllowed::class.java) ?: return true

                rolesAllowed.value.any { identityHolder.hasRole(it) }
            }
            else -> false
        }

    }
}