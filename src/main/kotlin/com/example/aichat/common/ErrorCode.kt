package com.example.aichat.common

import org.springframework.http.HttpStatus

enum class ErrorCode(
	val status: HttpStatus,
	val code: String,
	val defaultMessage: String
) {
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Invalid request"),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Unauthorized"),
	FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "Forbidden"),
	NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "Not found"),
	CONFLICT(HttpStatus.CONFLICT, "CONFLICT", "Conflict"),
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed"),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Internal server error")
}
