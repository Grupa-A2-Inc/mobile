package com.adaptive_tutor_mobile.domain.model.courses

import com.adaptive_tutor_mobile.data.remote.dto.CompletedCourseDto
import com.adaptive_tutor_mobile.data.remote.dto.EnrolledCourseDto

data class EnrolledCourse(
    val courseId: String,
    val courseTitle: String,
    val courseCategory: String?,
    val progressPercent: Double,
    val enrolledAt: String,
    val completedAt: String?
) {
    val isCompleted: Boolean get() = !completedAt.isNullOrBlank()
}

fun EnrolledCourseDto.toDomain(): EnrolledCourse = EnrolledCourse(
    courseId        = courseId,
    courseTitle     = courseTitle,
    courseCategory  = courseCategory,
    progressPercent = progressPercent ?: 0.0,
    enrolledAt      = enrolledAt,
    completedAt     = completedAt?.takeIf { it.isNotBlank() }
)

fun CompletedCourseDto.toDomain(): EnrolledCourse = EnrolledCourse(
    courseId        = courseId,
    courseTitle     = courseTitle,
    courseCategory  = null,
    progressPercent = 100.0,
    enrolledAt      = enrolledAt,
    completedAt     = completedAt
)
