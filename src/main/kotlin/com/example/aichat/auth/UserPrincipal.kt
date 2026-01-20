package com.example.aichat.auth

import com.example.aichat.user.UserRole
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.UUID

class UserPrincipal(
	val userId: UUID,
	val email: String,
	val role: UserRole
) {
	val authorities: Collection<GrantedAuthority> = listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
}
