package com.example.aichat.report

import com.example.aichat.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "activity_logs")
class ActivityLogEntity(
	@Column(nullable = false)
	var userId: UUID,

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	var type: ActivityLogType
) : BaseEntity() {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	var id: UUID? = null
}
