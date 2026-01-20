package com.example.aichat.report.controller

import com.example.aichat.report.dto.DailyStatsResponse
import com.example.aichat.report.service.ActivityLogService
import com.example.aichat.report.service.ReportService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class ReportController(
	private val activityLogService: ActivityLogService,
	private val reportService: ReportService
) {
	@GetMapping("/stats")
	@PreAuthorize("hasRole('ADMIN')")
	fun stats(): DailyStatsResponse {
		return activityLogService.dailyStats()
	}

	@GetMapping("/reports/chats")
	@PreAuthorize("hasRole('ADMIN')")
	fun report(): ResponseEntity<String> {
		val csv = reportService.buildDailyReportCsv()
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=chat-report.csv")
			.contentType(MediaType.parseMediaType("text/csv"))
			.body(csv)
	}
}
