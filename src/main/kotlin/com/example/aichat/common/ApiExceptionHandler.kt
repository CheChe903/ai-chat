package com.example.aichat.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
	@ExceptionHandler(IllegalArgumentException::class)
	fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiError> {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ApiError("BAD_REQUEST", ex.message ?: "Invalid request"))
	}

	@ExceptionHandler(MethodArgumentNotValidException::class)
	fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
		val message = ex.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ApiError("VALIDATION_ERROR", message))
	}
}

class ApiError(
	val code: String,
	val message: String
)
