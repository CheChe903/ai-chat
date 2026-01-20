package com.example.aichat.feedback

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FeedbackRepository : JpaRepository<FeedbackEntity, UUID> {
	fun existsByUserIdAndChatId(userId: UUID, chatId: UUID): Boolean
	fun findAllByUserId(userId: UUID, pageable: Pageable): Page<FeedbackEntity>
	fun findAllByUserIdAndIsPositive(userId: UUID, isPositive: Boolean, pageable: Pageable): Page<FeedbackEntity>
	fun findAllByIsPositive(isPositive: Boolean, pageable: Pageable): Page<FeedbackEntity>
}
