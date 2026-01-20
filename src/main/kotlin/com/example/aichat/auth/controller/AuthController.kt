package com.example.aichat.auth.controller

import com.example.aichat.auth.dto.AuthResponse
import com.example.aichat.auth.dto.LoginRequest
import com.example.aichat.auth.dto.SignupRequest
import com.example.aichat.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
	private val authService: AuthService
) {
	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	fun signup(@Valid @RequestBody request: SignupRequest): AuthResponse {
		return authService.signup(request)
	}

	@PostMapping("/login")
	fun login(@Valid @RequestBody request: LoginRequest): AuthResponse {
		return authService.login(request)
	}
}
