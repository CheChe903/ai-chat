package com.example.aichat.auth.controller

import com.example.aichat.auth.dto.AuthResponse
import com.example.aichat.auth.dto.LoginRequest
import com.example.aichat.auth.dto.SignupRequest
import com.example.aichat.auth.service.AuthService
import com.example.aichat.common.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
	private val authService: AuthService
) {
	@PostMapping("/signup")
	fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<ApiResponse<AuthResponse>> {
		val response = authService.signup(request)
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response))
	}

	@PostMapping("/login")
	fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<AuthResponse> {
		return ApiResponse.ok(authService.login(request))
	}
}
