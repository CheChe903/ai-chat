package com.example.aichat.report.dto

class DailyStatsResponse(
	val signups: Long,
	val logins: Long,
	val chats: Long
)

class ReportResponse(
	val fileName: String,
	val contentType: String
)
