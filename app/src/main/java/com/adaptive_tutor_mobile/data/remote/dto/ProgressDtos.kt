package com.adaptive_tutor_mobile.data.remote.dto


import com.google.gson.annotations.SerializedName

/**
 * Răspuns pentru GET /api/v1/courses/{courseId}/my-progress
 */
data class ProgressWithLessonListDto(
    val totalLessons: Int,
    val visitedLessons: Int,
    val progressPercent: Double,
    val completedAt: String?,           // ISO LocalDateTime, null = în progres
    val lessons: List<LessonStatusDto> = emptyList()
)

data class LessonStatusDto(
    val lessonId: String,
    val title: String,
    val visited: Boolean,
    val visitedAt: String?              // ISO LocalDateTime, null dacă nu a fost vizitată
)

/**
 * Răspuns pentru GET /api/v1/students/me/completed-courses
 * Are mai puține câmpuri decât EnrolledCourseDto (fără category, fără progressPercent).
 * Le mapăm la EnrolledCourse cu progressPercent=100.0 și completedAt populat.
 */
data class CompletedCourseDto(
    val courseId: String,
    val courseTitle: String,
    val enrolledAt: String,
    val completedAt: String
)

/**
 * Spring Page wrapper. Backend-ul returnează paginat pentru toate listele.
 * Folosim doar `content`; restul câmpurilor sunt pentru pagination UI viitor.
 */
data class PageDto<T>(
    val content: List<T> = emptyList(),
    val totalElements: Int = 0,
    val totalPages: Int = 0,
    val number: Int = 0,
    val size: Int = 0,
    val first: Boolean = true,
    val last: Boolean = true,
    val empty: Boolean = true
)