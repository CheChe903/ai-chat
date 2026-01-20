package com.example.aichat.report

import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class ActivityLogService(
	private val activityLogRepository: ActivityLogRepository
) {
	fun record(userId: UUID, type: ActivityLogType) {
		activityLogRepository.save(ActivityLogEntity(userId = userId, type = type))
	}

	fun dailyStats(): DailyStatsResponse {
		val end = Instant.now()
		val start = end.minus(1, ChronoUnit.DAYS)
		return DailyStatsResponse(
			signups = activityLogRepository.countByTypeBetween(ActivityLogType.SIGNUP, start, end),
			logins = activityLogRepository.countByTypeBetween(ActivityLogType.LOGIN, start, end),
			chats = activityLogRepository.countByTypeBetween(ActivityLogType.CHAT, start, end)
		)
	}
}
