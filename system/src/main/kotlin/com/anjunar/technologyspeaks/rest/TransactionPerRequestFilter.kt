package com.anjunar.technologyspeaks.rest

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class TransactionPerRequestFilter(
    private val txManager: PlatformTransactionManager
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return ! path.startsWith("/service")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val def = DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED).apply {
            isReadOnly = request.method == "GET" || request.method == "HEAD"
        }

        try {
            filterChain.doFilter(request, response)
            val status = txManager.getTransaction(def)

            if (! status.isRollbackOnly) {
                txManager.commit(status)
            }
        } catch (t: Throwable) {
            val status = txManager.getTransaction(def)
            txManager.rollback(status)
            throw t
        }
    }
}
