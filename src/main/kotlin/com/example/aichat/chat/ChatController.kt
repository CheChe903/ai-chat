package com.example.aichat.chat

import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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

@RestController
@RequestMapping("/api")
class ChatController(
	private val chatService: ChatService
) {
	@PostMapping("/chats", produces = [MediaType.APPLICATION_JSON_VALUE])
	fun createChat(@Valid @RequestBody request: ChatCreateRequest): ChatResponse {
		if (request.isStreaming) {
			throw IllegalArgumentException("Streaming request should use Accept: text/event-stream")
		}
		return chatService.createChat(request)
	}

	@PostMapping("/chats", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
	fun streamChat(@Valid @RequestBody request: ChatCreateRequest): SseEmitter {
		if (!request.isStreaming) {
			throw IllegalArgumentException("isStreaming=true is required for SSE")
		}
		val emitter = SseEmitter(Duration.ofMinutes(5).toMillis())
		Thread {
			try {
				val response = chatService.streamChat(request) { chunk ->
					emitter.send(SseEmitter.event().name("delta").data(chunk))
				}
				emitter.send(SseEmitter.event().name("complete").data(response))
				emitter.complete()
			} catch (ex: Exception) {
				emitter.completeWithError(ex)
			}
		}.start()
		return emitter
	}

	@GetMapping("/chats")
	fun listChats(
		@RequestParam(defaultValue = "0") page: Int,
		@RequestParam(defaultValue = "20") size: Int,
		@RequestParam(defaultValue = "desc") sort: String
	): ThreadChatPageResponse {
		val direction = if (sort.equals("asc", ignoreCase = true)) {
			org.springframework.data.domain.Sort.Direction.ASC
		} else {
			org.springframework.data.domain.Sort.Direction.DESC
		}
		return chatService.listChats(page, size, direction)
	}

	@DeleteMapping("/threads/{threadId}")
	fun deleteThread(@PathVariable threadId: UUID): ResponseEntity<Void> {
		chatService.deleteThread(threadId)
		return ResponseEntity.noContent().build()
	}
}
