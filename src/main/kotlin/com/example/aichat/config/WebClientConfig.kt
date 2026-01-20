package com.example.aichat.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import java.util.concurrent.TimeUnit

@Configuration
class WebClientConfig(
	private val appProperties: AppProperties
) {
	@Bean
	fun webClient(): WebClient {
		val httpClient = HttpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, appProperties.openai.connectTimeoutMs.toInt())
			.doOnConnected { conn ->
				conn.addHandlerLast(ReadTimeoutHandler(appProperties.openai.readTimeoutMs, TimeUnit.MILLISECONDS))
			}

		return WebClient.builder()
			.baseUrl(appProperties.openai.baseUrl)
			.clientConnector(ReactorClientHttpConnector(httpClient))
			.build()
	}
}
