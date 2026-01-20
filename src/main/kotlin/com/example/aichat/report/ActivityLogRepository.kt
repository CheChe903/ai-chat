package com.example.aichat.report

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface ActivityLogRepository : JpaRepository<ActivityLogEntity, UUID> {
	@Query("select count(a) from ActivityLogEntity a where a.type = :type and a.createdAt between :start and :end")
	fun countByTypeBetween(@Param("type") type: ActivityLogType, @Param("start") start: Instant, @Param("end") end: Instant): Long
}
