package com.example.aichat.openai

import com.example.aichat.config.AppProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class OpenAiClient(
	private val appProperties: AppProperties,
	private val webClient: WebClient
) {
	fun createChatCompletion(
		messages: List<ChatMessage>,
		modelOverride: String?
	): ChatCompletionResponse {
		val apiKey = appProperties.openai.apiKey
		if (apiKey.isBlank()) {
			return ChatCompletionResponse(
				choices = listOf(
					Choice(message = ChatMessage(role = "assistant", content = "OPENAI_API_KEY not configured"))
				)
			)
		}
		val request = ChatCompletionRequest(
			model = modelOverride ?: appProperties.openai.model,
			messages = messages,
			stream = false
		)
		return webClient.post()
			.uri("/v1/chat/completions")
			.header("Authorization", "Bearer $apiKey")
			.bodyValue(request)
			.retrieve()
			.bodyToMono(ChatCompletionResponse::class.java)
			.onErrorResume { Mono.just(ChatCompletionResponse.error(it.message ?: "openai error")) }
			.block() ?: ChatCompletionResponse.error("openai response empty")
	}
}

class ChatCompletionRequest(
	val model: String,
	val messages: List<ChatMessage>,
	val stream: Boolean
)

class ChatMessage(
	val role: String,
	val content: String
)

class ChatCompletionResponse(
	val choices: List<Choice>
) {
	companion object {
		fun error(message: String): ChatCompletionResponse {
			return ChatCompletionResponse(
				choices = listOf(Choice(ChatMessage("assistant", message)))
			)
		}
	}
}

class Choice(
	val message: ChatMessage
)
