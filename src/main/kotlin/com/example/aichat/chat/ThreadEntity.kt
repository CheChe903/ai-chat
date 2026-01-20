package com.example.aichat.chat

import com.example.aichat.common.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "threads")
class ThreadEntity(
	@Column(nullable = false)
	var userId: UUID,

	@Column(nullable = false)
	var lastQuestionAt: Instant
) : BaseEntity() {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	var id: UUID? = null

	@OneToMany(mappedBy = "thread", cascade = [CascadeType.ALL], orphanRemoval = true)
	val chats: MutableList<ChatEntity> = mutableListOf()
}
