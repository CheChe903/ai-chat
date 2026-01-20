package com.example.aichat.chat.controller

import com.example.aichat.chat.dto.ChatCreateRequest
import com.example.aichat.chat.dto.ChatResponse
import com.example.aichat.chat.dto.ThreadChatPageResponse
import com.example.aichat.chat.service.ChatService
import com.example.aichat.common.ApiException
import com.example.aichat.common.ApiResponse
import com.example.aichat.common.ErrorCode
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.Duration
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

@RestController
@RequestMapping("/api")
class ChatController(
	private val chatService: ChatService
) {
	@PostMapping("/chats", produces = [MediaType.APPLICATION_JSON_VALUE])
	fun createChat(@Valid @RequestBody request: ChatCreateRequest): ApiResponse<ChatResponse> {
		if (request.isStreaming) {
			throw ApiException(ErrorCode.BAD_REQUEST, "Streaming request should use Accept: text/event-stream")
		}
		return ApiResponse.ok(chatService.createChat(request))
	}

	@PostMapping("/chats", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
	fun streamChat(@Valid @RequestBody request: ChatCreateRequest): SseEmitter {
		if (!request.isStreaming) {
			throw ApiException(ErrorCode.BAD_REQUEST, "isStreaming=true is required for SSE")
		}
		val emitter = SseEmitter(Duration.ofMinutes(5).toMillis())
		val closed = AtomicBoolean(false)
		val session = chatService.streamChat(
			request,
			onChunk = { chunk ->
				if (!closed.get()) {
					emitter.send(SseEmitter.event().name("delta").data(ApiResponse.ok(chunk)))
				}
			},
			onComplete = { response ->
				if (!closed.getAndSet(true)) {
					emitter.send(SseEmitter.event().name("complete").data(ApiResponse.ok(response)))
					emitter.complete()
				}
			},
			onError = { ex ->
				if (!closed.getAndSet(true)) {
					emitter.send(SseEmitter.event().name("error")
						.data(ApiResponse.error(ErrorCode.INTERNAL_ERROR, ex.message)))
					emitter.complete()
				}
			}
		)

		emitter.onCompletion {
			closed.set(true)
			session.cancel()
			session.disposable.dispose()
		}
		emitter.onTimeout {
			closed.set(true)
			session.cancel()
			session.disposable.dispose()
		}
		emitter.onError {
			closed.set(true)
			session.cancel()
			session.disposable.dispose()
		}

		return emitter
	}

	@GetMapping("/chats")
	fun listChats(
		@RequestParam(defaultValue = "0") page: Int,
		@RequestParam(defaultValue = "20") size: Int,
		@RequestParam(defaultValue = "desc") sort: String
	): ApiResponse<ThreadChatPageResponse> {
		val direction = if (sort.equals("asc", ignoreCase = true)) {
			org.springframework.data.domain.Sort.Direction.ASC
		} else {
			org.springframework.data.domain.Sort.Direction.DESC
		}
		return ApiResponse.ok(chatService.listChats(page, size, direction))
	}

	@DeleteMapping("/threads/{threadId}")
	fun deleteThread(@PathVariable threadId: UUID): ApiResponse<Unit> {
		chatService.deleteThread(threadId)
		return ApiResponse.ok(null)
	}
}
