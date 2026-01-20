package com.example.aichat.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
class AppProperties(
	var jwt: Jwt = Jwt(),
	var openai: OpenAi = OpenAi()
) {
	class Jwt {
		var issuer: String = "ai-chat"
		var secret: String = "change-me-in-prod"
		var accessTokenMinutes: Long = 60
	}

	class OpenAi {
		var apiKey: String = ""
		var baseUrl: String = "https://api.openai.com"
		var model: String = "gpt-4o-mini"
		var connectTimeoutMs: Long = 30000
		var readTimeoutMs: Long = 60000
	}
}
