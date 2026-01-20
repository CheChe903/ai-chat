package com.example.aichat.auth

import com.example.aichat.config.AppProperties
import com.example.aichat.user.UserRole
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date
import java.util.UUID

@Service
class JwtService(
	private val appProperties: AppProperties
) {
	private val key = Keys.hmacShaKeyFor(appProperties.jwt.secret.toByteArray(StandardCharsets.UTF_8))

	fun createAccessToken(userId: UUID, email: String, role: UserRole): String {
		val now = Instant.now()
		val expiresAt = now.plusSeconds(appProperties.jwt.accessTokenMinutes * 60)
		return Jwts.builder()
			.subject(userId.toString())
			.issuer(appProperties.jwt.issuer)
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiresAt))
			.claim("role", role.name)
			.claim("email", email)
			.signWith(key)
			.compact()
	}

	fun parseToken(token: String): Claims {
		return Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.payload
	}

	fun toPrincipal(claims: Claims): UserPrincipal {
		val userId = UUID.fromString(claims.subject)
		val role = UserRole.valueOf(claims["role"].toString())
		val email = claims["email"].toString()
		return UserPrincipal(userId, email, role)
	}
}
