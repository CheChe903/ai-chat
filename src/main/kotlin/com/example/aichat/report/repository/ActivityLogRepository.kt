package com.example.aichat.report.repository

import com.example.aichat.report.domain.ActivityLog
import com.example.aichat.report.domain.ActivityLogType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface ActivityLogRepository : JpaRepository<ActivityLog, UUID> {
	@Query("select count(a) from ActivityLog a where a.type = :type and a.createdAt between :start and :end")
	fun countByTypeBetween(@Param("type") type: ActivityLogType, @Param("start") start: Instant, @Param("end") end: Instant): Long
}
