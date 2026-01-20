package com.example.aichat.llm

import reactor.core.publisher.Flux

interface LlmClient {
	fun createChatCompletion(messages: List<LlmMessage>, modelOverride: String?): LlmCompletionResponse
	fun streamChatCompletion(messages: List<LlmMessage>, modelOverride: String?): Flux<String>
}

data class LlmMessage(
	val role: String,
	val content: String
)

data class LlmCompletionChoice(
	val message: LlmMessage
)

data class LlmCompletionResponse(
	val choices: List<LlmCompletionChoice>
)
