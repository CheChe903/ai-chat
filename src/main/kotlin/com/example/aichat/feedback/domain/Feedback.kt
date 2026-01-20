package com.example.aichat.feedback.domain

import com.example.aichat.chat.domain.Chat
import com.example.aichat.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(
	name = "feedbacks",
	uniqueConstraints = [UniqueConstraint(name = "uk_feedback_user_chat", columnNames = ["user_id", "chat_id"])]
)
class Feedback(
	@Column(name = "user_id", nullable = false)
	var userId: UUID,

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_id", nullable = false)
	var chat: Chat,

	@Column(name = "is_positive", nullable = false)
	var isPositive: Boolean,

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	var status: FeedbackStatus = FeedbackStatus.PENDING
) : BaseEntity() {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	var id: UUID? = null
}
