package com.example.aichat.chat.repository

import com.example.aichat.chat.domain.Chat
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ChatRepository : JpaRepository<Chat, UUID> {
	fun findByThreadId(threadId: UUID, sort: Sort): List<Chat>
}
