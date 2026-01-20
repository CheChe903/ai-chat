package com.example.aichat

import com.example.aichat.config.AppProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(AppProperties::class)
class AiChatApplication

fun main(args: Array<String>) {
	runApplication<AiChatApplication>(*args)
}
