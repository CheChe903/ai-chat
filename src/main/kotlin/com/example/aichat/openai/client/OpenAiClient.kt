package com.example.aichat.openai.client

import com.example.aichat.config.AppProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class OpenAiClient(
	private val appProperties: AppProperties,
	private val webClient: WebClient,
	private val objectMapper: ObjectMapper
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

	fun streamChatCompletion(
		messages: List<ChatMessage>,
		modelOverride: String?
	): Flux<String> {
		val apiKey = appProperties.openai.apiKey
		if (apiKey.isBlank()) {
			return Flux.just("OPENAI_API_KEY not configured")
		}
		val request = ChatCompletionRequest(
			model = modelOverride ?: appProperties.openai.model,
			messages = messages,
			stream = true
		)
		return webClient.post()
			.uri("/v1/chat/completions")
			.header("Authorization", "Bearer $apiKey")
			.accept(MediaType.TEXT_EVENT_STREAM)
			.bodyValue(request)
			.retrieve()
			.bodyToFlux(String::class.java)
			.flatMap { chunk -> Flux.fromIterable(parseStreamChunk(chunk)) }
			.onErrorResume { Flux.just("[openai error] ${it.message ?: "unknown"}") }
	}

	private fun parseStreamChunk(chunk: String): List<String> {
		val results = mutableListOf<String>()
		val lines = chunk.split("\n")
		for (line in lines) {
			val trimmed = line.trim()
			if (!trimmed.startsWith("data:")) {
				continue
			}
			val data = trimmed.removePrefix("data:").trim()
			if (data == "[DONE]") {
				continue
			}
			try {
				val node = objectMapper.readTree(data)
				val content = node["choices"]?.get(0)?.get("delta")?.get("content")?.asText()
				if (!content.isNullOrBlank()) {
					results.add(content)
				}
			} catch (_: Exception) {
				// ignore malformed chunks
			}
		}
		return results
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
