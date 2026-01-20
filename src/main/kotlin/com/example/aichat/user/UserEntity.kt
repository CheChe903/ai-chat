package com.example.aichat.user

import com.example.aichat.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(
	@Column(nullable = false, unique = true)
	var email: String,

	@Column(nullable = false)
	var passwordHash: String,

	@Column(nullable = false)
	var name: String,

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	var role: UserRole = UserRole.MEMBER
) : BaseEntity() {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	var id: UUID? = null
}
