package com.example.aichat.auth.service

import com.example.aichat.auth.dto.AuthResponse
import com.example.aichat.auth.dto.LoginRequest
import com.example.aichat.auth.dto.SignupRequest
import com.example.aichat.auth.security.JwtService
import com.example.aichat.report.domain.ActivityLogType
import com.example.aichat.report.service.ActivityLogService
import com.example.aichat.user.domain.User
import com.example.aichat.user.domain.UserRole
import com.example.aichat.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
	private val userRepository: UserRepository,
	private val passwordEncoder: PasswordEncoder,
	private val jwtService: JwtService,
	private val activityLogService: ActivityLogService
) {
	fun signup(request: SignupRequest): AuthResponse {
		if (userRepository.existsByEmail(request.email)) {
			throw IllegalArgumentException("Email already registered")
		}
		val user = User(
			email = request.email,
			passwordHash = passwordEncoder.encode(request.password),
			name = request.name,
			role = UserRole.MEMBER
		)
		val saved = userRepository.save(user)
		val token = jwtService.createAccessToken(saved.id!!, saved.email, saved.role)
		activityLogService.record(saved.id!!, ActivityLogType.SIGNUP)
		return AuthResponse(token, saved.id.toString(), saved.role.name)
	}

	fun login(request: LoginRequest): AuthResponse {
		val user = userRepository.findByEmail(request.email)
			?: throw IllegalArgumentException("Invalid credentials")
		if (!passwordEncoder.matches(request.password, user.passwordHash)) {
			throw IllegalArgumentException("Invalid credentials")
		}
		val token = jwtService.createAccessToken(user.id!!, user.email, user.role)
		activityLogService.record(user.id!!, ActivityLogType.LOGIN)
		return AuthResponse(token, user.id.toString(), user.role.name)
	}
}
