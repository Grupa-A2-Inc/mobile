package com.adaptive_tutor_mobile
import com.adaptive_tutor_mobile.data.remote.dto.CompletedCourseDto
import com.adaptive_tutor_mobile.data.remote.dto.EnrolledCourseDto
import com.adaptive_tutor_mobile.data.remote.dto.LessonStatusDto
import com.adaptive_tutor_mobile.data.remote.dto.ProgressWithLessonListDto
import com.adaptive_tutor_mobile.domain.model.courses.EnrolledCourse

object ProgressTestFixtures {
    fun enrolledDto(
        unrollmentId: String = "enr-1",
        courseId: String = "course-1",
        title: String = "Matematică",
        category: String? = "STEM",
        progress: Double? = 42.5,
        enrolledAt: String = "2025-01-10T09:00:00",
        completedAt: String? = null,
        visibility: String? = null
    ) = EnrolledCourseDto(
        unrollmentId = unrollmentId,
        courseId = courseId,
        courseTitle = title,
        courseCategory = category,
        progressPercent = progress,
        enrolledAt = enrolledAt,
        completedAt = completedAt,
        courseVisibility = visibility
    )

    fun completedDto(
        courseId: String = "course-2",
        title: String = "Fizică",
        enrolledAt: String = "2024-09-01T10:00:00",
        completedAt: String = "2025-01-15T18:30:00"
    ) = CompletedCourseDto(
        courseId = courseId,
        courseTitle = title,
        enrolledAt = enrolledAt,
        completedAt = completedAt
    )

    fun domainCourse(
        enrollmentId: String = "enr-1",
        courseId: String = "course-1",
        title: String = "Matematică",
        category: String? = "STEM",
        progress: Double = 42.5,
        completedAt: String? = null,
        courseVisibility: String? = null
    ) = EnrolledCourse(
        enrollmentId = enrollmentId,
        courseId = courseId,
        courseTitle = title,
        courseCategory = category,
        progressPercent = progress,
        enrolledAt = "2025-01-10T09:00:00",
        completedAt = completedAt,
        courseVisibility = courseVisibility
    )

    fun progressDto(
        totalLessons: Int = 10,
        visitedLessons: Int = 4,
        progressPercent: Double = 40.0,
        completedAt: String? = null
    ) = ProgressWithLessonListDto(
        totalLessons = totalLessons,
        visitedLessons = visitedLessons,
        progressPercent = progressPercent,
        completedAt = completedAt,
        lessons = listOf(
            LessonStatusDto("l1", "Lecția 1", visited = true,  visitedAt = "2025-01-10T10:00:00"),
            LessonStatusDto("l2", "Lecția 2", visited = false, visitedAt = null)
        )
    )
}