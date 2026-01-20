package com.example.aichat.common

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import java.time.Instant

@MappedSuperclass
open class BaseEntity {
	@Column(nullable = false, updatable = false)
	var createdAt: Instant = Instant.EPOCH

	@PrePersist
	fun onCreate() {
		createdAt = Instant.now()
	}
}
