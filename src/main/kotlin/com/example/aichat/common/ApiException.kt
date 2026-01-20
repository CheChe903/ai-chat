package com.example.aichat.common

class ApiException(
	val errorCode: ErrorCode,
	override val message: String? = null
) : RuntimeException(message)
