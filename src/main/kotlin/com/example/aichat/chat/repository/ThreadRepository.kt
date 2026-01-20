package com.example.aichat.chat.repository

import com.example.aichat.chat.domain.Thread
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ThreadRepository : JpaRepository<Thread, UUID> {
	fun findTopByUserIdOrderByLastQuestionAtDesc(userId: UUID): Thread?
	fun findAllByUserId(userId: UUID, pageable: Pageable): Page<Thread>
	fun existsByIdAndUserId(id: UUID, userId: UUID): Boolean
}
