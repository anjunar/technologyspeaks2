package com.anjunar.technologyspeaks.rest

import com.anjunar.json.mapper.ErrorRequest
import com.anjunar.json.mapper.ErrorRequestException
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice


@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ErrorRequestException::class)
    fun handleBusinessException(ex: ErrorRequestException): ResponseEntity<List<ErrorRequest>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ex.errors)
    }

}