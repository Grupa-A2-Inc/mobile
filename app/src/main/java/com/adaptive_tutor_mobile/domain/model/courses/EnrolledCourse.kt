package com.adaptive_tutor_mobile.domain.model.courses

import com.adaptive_tutor_mobile.data.remote.dto.CompletedCourseDto
import com.adaptive_tutor_mobile.data.remote.dto.EnrolledCourseDto

data class EnrolledCourse(
    val enrollmentId: String,
    val courseId: String,
    val courseTitle: String,
    val courseCategory: String?,
    val progressPercent: Double,
    val enrolledAt: String,
    val completedAt: String?,
    val courseVisibility: String?,
    val canUnenroll: Boolean = true
) {
    val isCompleted: Boolean get() = !completedAt.isNullOrBlank()
    val isPublic: Boolean get() = courseVisibility?.uppercase() == "PUBLIC"
    val canDownloadCertificate: Boolean get() = isCompleted && isPublic
}

fun EnrolledCourseDto.toDomain(): EnrolledCourse = EnrolledCourse(
    enrollmentId    = unrollmentId,
    courseId        = courseId,
    courseTitle     = courseTitle,
    courseCategory  = courseCategory,
    progressPercent = progressPercent ?: 0.0,
    enrolledAt      = enrolledAt,
    completedAt     = completedAt?.takeIf { it.isNotBlank() },
    courseVisibility = courseVisibility,
    // canUnenroll este setat de repository dupa cross-reference cu cursurile publice
    canUnenroll     = false
)

fun CompletedCourseDto.toDomain(): EnrolledCourse = EnrolledCourse(
    enrollmentId    = "",
    courseId        = courseId,
    courseTitle     = courseTitle,
    courseCategory  = null,
    progressPercent = 100.0,
    enrolledAt      = enrolledAt,
    completedAt     = completedAt,
    courseVisibility = null
)
