package com.example.aichat.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
	private val jwtService: JwtService
) : OncePerRequestFilter() {
	override fun doFilterInternal(
		request: HttpServletRequest,
		response: HttpServletResponse,
		filterChain: FilterChain
	) {
		val authHeader = request.getHeader("Authorization")
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			val token = authHeader.removePrefix("Bearer ").trim()
			try {
				val claims = jwtService.parseToken(token)
				val principal = jwtService.toPrincipal(claims)
				val authentication = UsernamePasswordAuthenticationToken(principal, token, principal.authorities)
				SecurityContextHolder.getContext().authentication = authentication
			} catch (ex: Exception) {
				SecurityContextHolder.clearContext()
			}
		}
		filterChain.doFilter(request, response)
	}
}
