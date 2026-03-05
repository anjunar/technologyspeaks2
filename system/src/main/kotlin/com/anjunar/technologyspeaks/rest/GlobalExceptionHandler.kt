package com.anjunar.technologyspeaks.rest

import com.anjunar.json.mapper.ErrorRequest
import com.anjunar.json.mapper.ErrorRequestException
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice


@RestControllerAdvice
class GlobalExceptionHandler(val txManager: PlatformTransactionManager) {

    @ExceptionHandler(ErrorRequestException::class)
    fun handleBusinessException(ex: ErrorRequestException): ResponseEntity<List<ErrorRequest>> {

        val def = DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED)

        val transaction = txManager.getTransaction(def)

        txManager.rollback(transaction)

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ex.errors)
    }

}