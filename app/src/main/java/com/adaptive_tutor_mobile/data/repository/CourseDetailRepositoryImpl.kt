package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.CourseDetailApi
import com.adaptive_tutor_mobile.data.remote.dto.ChapterFullViewDTO
import com.adaptive_tutor_mobile.data.remote.dto.LessonFullViewDTO
import com.adaptive_tutor_mobile.data.remote.dto.ResponseCourseFullViewDto
import com.adaptive_tutor_mobile.domain.model.Chapter
import com.adaptive_tutor_mobile.domain.model.CourseDetail
import com.adaptive_tutor_mobile.domain.model.LessonSummary
import com.adaptive_tutor_mobile.domain.repository.CourseDetailRepository
import javax.inject.Inject

class CourseDetailRepositoryImpl @Inject constructor(
    private val api: CourseDetailApi
) : CourseDetailRepository {

    override suspend fun getCourseFullView(courseId: String): Result<CourseDetail> {
        return runCatching {
            val dto = api.getCourseFullView(courseId)
            dto.toDomain()
        }
    }

    // Mapping helpers

    private fun ResponseCourseFullViewDto.toDomain() =
        CourseDetail(
            id          = id,
            title       = title,
            description = description ?: "",
            chapters    = chapters
                .sortedBy { it.orderIndex }
                .map { it.toDomain() }
        )

    private fun ChapterFullViewDTO.toDomain() =
        Chapter(
            id      = id,
            title   = title,
            lessons = lessons
                .sortedBy { it.orderIndex }
                .map { it.toDomain() }
        )

    private fun LessonFullViewDTO.toDomain() =
        LessonSummary(
            id      = id,
            title   = title,
            hasTest = testId != null
        )
}