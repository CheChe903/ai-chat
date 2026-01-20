package com.example.aichat.chat.service

import com.example.aichat.chat.domain.Chat
import com.example.aichat.chat.domain.Thread
import com.example.aichat.chat.dto.ChatCreateRequest
import com.example.aichat.chat.dto.ChatResponse
import com.example.aichat.chat.dto.ThreadChatPageResponse
import com.example.aichat.chat.dto.ThreadChatResponse
import com.example.aichat.chat.repository.ChatRepository
import com.example.aichat.chat.repository.ThreadRepository
import com.example.aichat.common.ApiException
import com.example.aichat.common.ErrorCode
import com.example.aichat.common.SecurityUtil
import com.example.aichat.openai.client.ChatMessage
import com.example.aichat.openai.client.OpenAiClient
import com.example.aichat.report.domain.ActivityLogType
import com.example.aichat.report.service.ActivityLogService
import com.example.aichat.user.domain.UserRole
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
@Transactional
class ChatService(
	private val threadRepository: ThreadRepository,
	private val chatRepository: ChatRepository,
	private val openAiClient: OpenAiClient,
	private val activityLogService: ActivityLogService
) {
	private val threadTimeout = Duration.ofMinutes(30)

	fun createChat(request: ChatCreateRequest): ChatResponse {
		val principal = currentUser()
		val now = Instant.now()
		val thread = resolveThread(principal.userId, now)
		val messages = buildMessages(thread, request.question)
		val completion = openAiClient.createChatCompletion(messages, request.model)
		val answer = completion.choices.firstOrNull()?.message?.content ?: ""

		thread.lastQuestionAt = now
		activityLogService.record(principal.userId, ActivityLogType.CHAT)
		val chat = Chat(
			thread = thread,
			question = request.question,
			answer = answer
		)
		val saved = chatRepository.save(chat)
		return saved.toResponse()
	}

	fun streamChat(request: ChatCreateRequest, onChunk: (String) -> Unit): ChatResponse {
		val principal = currentUser()
		val now = Instant.now()
		val thread = resolveThread(principal.userId, now)
		val messages = buildMessages(thread, request.question)

		val chunks = openAiClient.streamChatCompletion(messages, request.model)
			.doOnNext { onChunk(it) }
			.collectList()
			.block() ?: emptyList()
		val answer = chunks.joinToString("")

		thread.lastQuestionAt = now
		activityLogService.record(principal.userId, ActivityLogType.CHAT)
		val chat = Chat(
			thread = thread,
			question = request.question,
			answer = answer
		)
		val saved = chatRepository.save(chat)
		return saved.toResponse()
	}

	fun listChats(page: Int, size: Int, sort: Sort.Direction): ThreadChatPageResponse {
		val principal = currentUser()
		val pageable = PageRequest.of(page, size, Sort.by(sort, "lastQuestionAt"))
		val threadsPage = if (principal.role == UserRole.ADMIN) {
			threadRepository.findAll(pageable)
		} else {
			threadRepository.findAllByUserId(principal.userId, pageable)
		}

		val items = threadsPage.content.map { thread ->
			val chats = chatRepository.findByThreadId(thread.id!!, Sort.by(sort, "createdAt"))
			ThreadChatResponse(
				threadId = thread.id.toString(),
				lastQuestionAt = DateTimeFormatter.ISO_INSTANT.format(thread.lastQuestionAt),
				chats = chats.map { it.toResponse() }
			)
		}

		return ThreadChatPageResponse(
			page = threadsPage.number,
			size = threadsPage.size,
			totalElements = threadsPage.totalElements,
			totalPages = threadsPage.totalPages,
			items = items
		)
	}

	fun deleteThread(threadId: UUID) {
		val principal = currentUser()
		val exists = if (principal.role == UserRole.ADMIN) {
			threadRepository.existsById(threadId)
		} else {
			threadRepository.existsByIdAndUserId(threadId, principal.userId)
		}
		if (!exists) {
			throw ApiException(ErrorCode.NOT_FOUND, "Thread not found")
		}
		threadRepository.deleteById(threadId)
	}

	private fun resolveThread(userId: UUID, now: Instant): Thread {
		val latest = threadRepository.findTopByUserIdOrderByLastQuestionAtDesc(userId)
		if (latest == null) {
			return threadRepository.save(Thread(userId = userId, lastQuestionAt = now))
		}
		val gap = Duration.between(latest.lastQuestionAt, now)
		if (gap > threadTimeout) {
			return threadRepository.save(Thread(userId = userId, lastQuestionAt = now))
		}
		return latest
	}

	private fun buildMessages(thread: Thread, question: String): List<ChatMessage> {
		val history = chatRepository.findByThreadId(thread.id!!, Sort.by(Sort.Direction.ASC, "createdAt"))
		val messages = mutableListOf<ChatMessage>()
		for (chat in history) {
			messages.add(ChatMessage(role = "user", content = chat.question))
			messages.add(ChatMessage(role = "assistant", content = chat.answer))
		}
		messages.add(ChatMessage(role = "user", content = question))
		return messages
	}

	private fun Chat.toResponse(): ChatResponse {
		return ChatResponse(
			chatId = id.toString(),
			threadId = thread.id.toString(),
			question = question,
			answer = answer,
			createdAt = DateTimeFormatter.ISO_INSTANT.format(createdAt)
		)
	}

	private fun currentUser() = SecurityUtil.currentUser()
}
