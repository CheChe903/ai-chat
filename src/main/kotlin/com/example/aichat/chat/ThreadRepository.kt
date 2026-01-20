package com.example.aichat.chat

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ThreadRepository : JpaRepository<ThreadEntity, UUID> {
	fun findTopByUserIdOrderByLastQuestionAtDesc(userId: UUID): ThreadEntity?
	fun findAllByUserId(userId: UUID, pageable: Pageable): Page<ThreadEntity>
	fun existsByIdAndUserId(id: UUID, userId: UUID): Boolean
}
