package com.example.aichat.common

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
	@ExceptionHandler(ApiException::class)
	fun handleApiException(ex: ApiException): ResponseEntity<ApiResponse<Nothing>> {
		return ResponseEntity.status(ex.errorCode.status)
			.body(ApiResponse.error(ex.errorCode, ex.message))
	}

	@ExceptionHandler(MethodArgumentNotValidException::class)
	fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
		val message = ex.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
		return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.status)
			.body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, message))
	}

	@ExceptionHandler(Exception::class)
	fun handleUnexpected(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
		return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.status)
			.body(ApiResponse.error(ErrorCode.INTERNAL_ERROR, ex.message))
	}
}
