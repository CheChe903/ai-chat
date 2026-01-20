package com.example.aichat.report.service

import com.example.aichat.chat.repository.ChatRepository
import org.springframework.stereotype.Service
import java.io.StringWriter
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class ReportService(
	private val chatRepository: ChatRepository
) {
	fun buildDailyReportCsv(): String {
		val end = Instant.now()
		val start = end.minus(1, ChronoUnit.DAYS)
		val chats = chatRepository.findAll()
		val writer = StringWriter()
		writer.appendLine("chat_id,thread_id,user_id,question,answer,created_at")
		for (chat in chats) {
			if (chat.createdAt.isBefore(start) || chat.createdAt.isAfter(end)) {
				continue
			}
			writer.appendLine(
				listOf(
					chat.id.toString(),
					chat.thread.id.toString(),
					chat.thread.userId.toString(),
					escape(chat.question),
					escape(chat.answer),
					chat.createdAt.toString()
				).joinToString(",")
			)
		}
		return writer.toString()
	}

	private fun escape(value: String): String {
		val needsWrap = value.contains(",") || value.contains("\n") || value.contains("\"")
		if (!needsWrap) {
			return value
		}
		return "\"" + value.replace("\"", "\"\"") + "\""
	}
}
