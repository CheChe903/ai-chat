package com.example.aichat.openai.client

import com.example.aichat.config.AppProperties
import com.example.aichat.llm.LlmClient
import com.example.aichat.llm.LlmCompletionChoice
import com.example.aichat.llm.LlmCompletionResponse
import com.example.aichat.llm.LlmMessage
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Primary
@Component
class OpenAiClient(
	private val appProperties: AppProperties,
	private val webClient: WebClient,
	private val objectMapper: ObjectMapper
) : LlmClient {
	override fun createChatCompletion(
		messages: List<LlmMessage>,
		modelOverride: String?
	): LlmCompletionResponse {
		val apiKey = appProperties.openai.apiKey
		if (apiKey.isBlank()) {
			return LlmCompletionResponse(
				choices = listOf(
					LlmCompletionChoice(message = LlmMessage(role = "assistant", content = "OPENAI_API_KEY not configured"))
				)
			)
		}
		val request = OpenAiChatCompletionRequest(
			model = modelOverride ?: appProperties.openai.model,
			messages = messages,
			stream = false
		)
		val response = webClient.post()
			.uri("/v1/chat/completions")
			.header("Authorization", "Bearer $apiKey")
			.bodyValue(request)
			.retrieve()
			.bodyToMono(OpenAiChatCompletionResponse::class.java)
			.onErrorResume { Mono.just(OpenAiChatCompletionResponse.error(it.message ?: "openai error")) }
			.block()

		return response?.toLlmResponse() ?: LlmCompletionResponse(
			choices = listOf(LlmCompletionChoice(LlmMessage("assistant", "openai response empty")))
		)
	}

	override fun streamChatCompletion(
		messages: List<LlmMessage>,
		modelOverride: String?
	): Flux<String> {
		val apiKey = appProperties.openai.apiKey
		if (apiKey.isBlank()) {
			return Flux.just("OPENAI_API_KEY not configured")
		}
		val request = OpenAiChatCompletionRequest(
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
				val delta = node["choices"]?.get(0)?.get("delta")
				val content = delta?.get("content")?.asText()
				if (!content.isNullOrBlank()) {
					results.add(content)
				}
				val toolCalls = delta?.get("tool_calls")
				if (toolCalls != null && toolCalls.isArray) {
					for (toolCall in toolCalls) {
						val function = toolCall.get("function")
						val name = function?.get("name")?.asText()
						val arguments = function?.get("arguments")?.asText()
						val text = buildString {
							append("[tool_call")
							if (!name.isNullOrBlank()) {
								append(":").append(name)
							}
							if (!arguments.isNullOrBlank()) {
								append(" ").append(arguments)
							}
							append("]")
						}
						results.add(text)
					}
				}
			} catch (_: Exception) {
				// ignore malformed chunks
			}
		}
		return results
	}
}

data class OpenAiChatCompletionRequest(
	val model: String,
	val messages: List<LlmMessage>,
	val stream: Boolean
)

data class OpenAiChatCompletionResponse(
	val choices: List<OpenAiChoice>
) {
	companion object {
		fun error(message: String): OpenAiChatCompletionResponse {
			return OpenAiChatCompletionResponse(
				choices = listOf(OpenAiChoice(LlmMessage("assistant", message)))
			)
		}
	}

	fun toLlmResponse(): LlmCompletionResponse {
		return LlmCompletionResponse(
			choices = choices.map { LlmCompletionChoice(it.message) }
		)
	}
}

data class OpenAiChoice(
	val message: LlmMessage
)
