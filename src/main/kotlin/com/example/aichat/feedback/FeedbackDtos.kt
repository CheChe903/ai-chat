package com.example.aichat.feedback

import jakarta.validation.constraints.NotNull

class FeedbackCreateRequest(
	@field:NotNull
	val chatId: String,
	@field:NotNull
	val isPositive: Boolean
)

class FeedbackStatusUpdateRequest(
	@field:NotNull
	val status: FeedbackStatus
)

class FeedbackResponse(
	val feedbackId: String,
	val chatId: String,
	val userId: String,
	val isPositive: Boolean,
	val status: String,
	val createdAt: String
)

class FeedbackPageResponse(
	val page: Int,
	val size: Int,
	val totalElements: Long,
	val totalPages: Int,
	val items: List<FeedbackResponse>
)
