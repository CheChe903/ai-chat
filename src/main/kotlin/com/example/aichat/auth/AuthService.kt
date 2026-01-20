package com.example.aichat.auth

import com.example.aichat.user.UserEntity
import com.example.aichat.user.UserRepository
import com.example.aichat.user.UserRole
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
	private val userRepository: UserRepository,
	private val passwordEncoder: PasswordEncoder,
	private val jwtService: JwtService
) {
	fun signup(request: SignupRequest): AuthResponse {
		if (userRepository.existsByEmail(request.email)) {
			throw IllegalArgumentException("Email already registered")
		}
		val user = UserEntity(
			email = request.email,
			passwordHash = passwordEncoder.encode(request.password),
			name = request.name,
			role = UserRole.MEMBER
		)
		val saved = userRepository.save(user)
		val token = jwtService.createAccessToken(saved.id!!, saved.email, saved.role)
		return AuthResponse(token, saved.id.toString(), saved.role.name)
	}

	fun login(request: LoginRequest): AuthResponse {
		val user = userRepository.findByEmail(request.email)
			?: throw IllegalArgumentException("Invalid credentials")
		if (!passwordEncoder.matches(request.password, user.passwordHash)) {
			throw IllegalArgumentException("Invalid credentials")
		}
		val token = jwtService.createAccessToken(user.id!!, user.email, user.role)
		return AuthResponse(token, user.id.toString(), user.role.name)
	}
}
