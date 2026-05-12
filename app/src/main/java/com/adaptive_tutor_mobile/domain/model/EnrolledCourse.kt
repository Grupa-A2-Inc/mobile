package com.adaptive_tutor_mobile.domain.model

import com.adaptive_tutor_mobile.data.remote.dto.CompletedCourseDto
import com.adaptive_tutor_mobile.data.remote.dto.EnrolledCourseDto

/**
 * Reprezentare uniformă a unui curs la care studentul e înscris (în progres sau completat).
 * - dacă `completedAt == null`  → curs activ; `progressPercent` poate fi orice între 0 și 100
 * - dacă `completedAt != null`  → curs completat; `progressPercent` este 100.0
 */
data class EnrolledCourse(
    val courseId: String,
    val courseTitle: String,
    val courseCategory: String?,
    val progressPercent: Double,
    val enrolledAt: String,             // ISO LocalDateTime
    val completedAt: String?            // ISO LocalDateTime sau null
) {
    val isCompleted: Boolean get() = completedAt != null
}

fun EnrolledCourseDto.toDomain(): EnrolledCourse = EnrolledCourse(
    courseId        = courseId,
    courseTitle     = courseTitle,
    courseCategory  = courseCategory,
    progressPercent = progressPercent ?: 0.0,
    enrolledAt      = enrolledAt,
    completedAt     = completedAt
)

fun CompletedCourseDto.toDomain(): EnrolledCourse = EnrolledCourse(
    courseId        = courseId,
    courseTitle     = courseTitle,
    courseCategory  = null,
    progressPercent = 100.0,
    enrolledAt      = enrolledAt,
    completedAt     = completedAt
)