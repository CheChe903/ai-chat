package com.example.aichat.feedback.controller

import com.example.aichat.feedback.dto.FeedbackCreateRequest
import com.example.aichat.feedback.dto.FeedbackPageResponse
import com.example.aichat.feedback.dto.FeedbackResponse
import com.example.aichat.feedback.dto.FeedbackStatusUpdateRequest
import com.example.aichat.feedback.service.FeedbackService
import jakarta.validation.Valid
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/feedbacks")
class FeedbackController(
	private val feedbackService: FeedbackService
) {
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@Valid @RequestBody request: FeedbackCreateRequest): FeedbackResponse {
		return feedbackService.createFeedback(request)
	}

	@GetMapping
	fun list(
		@RequestParam(defaultValue = "0") page: Int,
		@RequestParam(defaultValue = "20") size: Int,
		@RequestParam(defaultValue = "desc") sort: String,
		@RequestParam(required = false) positive: Boolean?
	): FeedbackPageResponse {
		val direction = if (sort.equals("asc", ignoreCase = true)) {
			Sort.Direction.ASC
		} else {
			Sort.Direction.DESC
		}
		return feedbackService.listFeedbacks(page, size, direction, positive)
	}

	@PatchMapping("/{feedbackId}/status")
	@PreAuthorize("hasRole('ADMIN')")
	fun updateStatus(
		@PathVariable feedbackId: UUID,
		@Valid @RequestBody request: FeedbackStatusUpdateRequest
	): FeedbackResponse {
		return feedbackService.updateStatus(feedbackId, request)
	}
}
