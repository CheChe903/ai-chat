package com.example.aichat.common

import com.example.aichat.auth.security.UserPrincipal
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtil {
	fun currentUser(): UserPrincipal {
		val auth = SecurityContextHolder.getContext().authentication
		val principal = auth?.principal
		if (principal !is UserPrincipal) {
			throw IllegalArgumentException("Unauthorized")
		}
		return principal
	}
}
