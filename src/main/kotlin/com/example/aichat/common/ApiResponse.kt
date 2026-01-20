package com.example.aichat.common

data class ApiResponse<T>(
	val success: Boolean,
	val data: T?,
	val error: ApiError?
) {
	companion object {
		fun <T> ok(data: T?): ApiResponse<T> = ApiResponse(true, data, null)

		fun error(code: ErrorCode, message: String?): ApiResponse<Nothing> {
			return ApiResponse(false, null, ApiError(code.code, message ?: code.defaultMessage))
		}
	}
}

data class ApiError(
	val code: String,
	val message: String
)
