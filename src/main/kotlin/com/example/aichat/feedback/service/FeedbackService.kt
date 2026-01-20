package com.example.aichat.feedback.service

import com.example.aichat.chat.repository.ChatRepository
import com.example.aichat.common.SecurityUtil
import com.example.aichat.feedback.domain.Feedback
import com.example.aichat.feedback.domain.FeedbackStatus
import com.example.aichat.feedback.dto.FeedbackCreateRequest
import com.example.aichat.feedback.dto.FeedbackPageResponse
import com.example.aichat.feedback.dto.FeedbackResponse
import com.example.aichat.feedback.dto.FeedbackStatusUpdateRequest
import com.example.aichat.feedback.repository.FeedbackRepository
import com.example.aichat.user.domain.UserRole
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class FeedbackService(
	private val feedbackRepository: FeedbackRepository,
	private val chatRepository: ChatRepository
) {
	fun createFeedback(request: FeedbackCreateRequest): FeedbackResponse {
		val principal = SecurityUtil.currentUser()
		val chatId = UUID.fromString(request.chatId)
		val chat = chatRepository.findById(chatId)
			.orElseThrow { IllegalArgumentException("Chat not found") }

		if (principal.role != UserRole.ADMIN && chat.thread.userId != principal.userId) {
			throw IllegalArgumentException("Forbidden")
		}

		if (feedbackRepository.existsByUserIdAndChatId(principal.userId, chatId)) {
			throw IllegalArgumentException("Feedback already exists")
		}

		val feedback = Feedback(
			userId = principal.userId,
			chat = chat,
			isPositive = request.isPositive,
			status = FeedbackStatus.PENDING
		)
		return feedbackRepository.save(feedback).toResponse()
	}

	fun listFeedbacks(page: Int, size: Int, sort: Sort.Direction, isPositive: Boolean?): FeedbackPageResponse {
		val principal = SecurityUtil.currentUser()
		val pageable = PageRequest.of(page, size, Sort.by(sort, "createdAt"))
		val pageResult = if (principal.role == UserRole.ADMIN) {
			if (isPositive == null) {
				feedbackRepository.findAll(pageable)
			} else {
				feedbackRepository.findAllByIsPositive(isPositive, pageable)
			}
		} else {
			if (isPositive == null) {
				feedbackRepository.findAllByUserId(principal.userId, pageable)
			} else {
				feedbackRepository.findAllByUserIdAndIsPositive(principal.userId, isPositive, pageable)
			}
		}

		return FeedbackPageResponse(
			page = pageResult.number,
			size = pageResult.size,
			totalElements = pageResult.totalElements,
			totalPages = pageResult.totalPages,
			items = pageResult.content.map { it.toResponse() }
		)
	}

	fun updateStatus(feedbackId: UUID, request: FeedbackStatusUpdateRequest): FeedbackResponse {
		val feedback = feedbackRepository.findById(feedbackId)
			.orElseThrow { IllegalArgumentException("Feedback not found") }
		feedback.status = request.status
		return feedbackRepository.save(feedback).toResponse()
	}

	private fun Feedback.toResponse(): FeedbackResponse {
		return FeedbackResponse(
			feedbackId = id.toString(),
			chatId = chat.id.toString(),
			userId = userId.toString(),
			isPositive = isPositive,
			status = status.name,
			createdAt = DateTimeFormatter.ISO_INSTANT.format(createdAt)
		)
	}
}
