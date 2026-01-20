package com.example.aichat.chat

import jakarta.validation.constraints.NotBlank

class ChatCreateRequest(
	@field:NotBlank
	val question: String,
	val isStreaming: Boolean = false,
	val model: String? = null
)

class ChatResponse(
	val chatId: String,
	val threadId: String,
	val question: String,
	val answer: String,
	val createdAt: String
)

class ThreadChatResponse(
	val threadId: String,
	val lastQuestionAt: String,
	val chats: List<ChatResponse>
)

class ThreadChatPageResponse(
	val page: Int,
	val size: Int,
	val totalElements: Long,
	val totalPages: Int,
	val items: List<ThreadChatResponse>
)
