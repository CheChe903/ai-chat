package com.example.aichat.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

class SignupRequest(
	@field:Email
	@field:NotBlank
	val email: String,
	@field:NotBlank
	val password: String,
	@field:NotBlank
	val name: String
)

class LoginRequest(
	@field:Email
	@field:NotBlank
	val email: String,
	@field:NotBlank
	val password: String
)

class AuthResponse(
	val accessToken: String,
	val userId: String,
	val role: String
)
