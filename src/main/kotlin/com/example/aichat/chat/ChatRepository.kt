package com.example.aichat.chat

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ChatRepository : JpaRepository<ChatEntity, UUID> {
	fun findByThreadId(threadId: UUID, sort: Sort): List<ChatEntity>
}
