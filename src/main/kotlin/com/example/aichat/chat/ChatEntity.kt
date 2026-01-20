package com.example.aichat.chat

import com.example.aichat.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "chats")
class ChatEntity(
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "thread_id", nullable = false)
	var thread: ThreadEntity,

	@Column(nullable = false, columnDefinition = "text")
	var question: String,

	@Column(nullable = false, columnDefinition = "text")
	var answer: String
) : BaseEntity() {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	var id: UUID? = null
}
