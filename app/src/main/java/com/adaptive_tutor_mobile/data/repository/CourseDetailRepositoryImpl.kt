package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.CourseDetailApi
import com.adaptive_tutor_mobile.data.remote.dto.ChapterFullViewDTO
import com.adaptive_tutor_mobile.data.remote.dto.LessonFullViewDTO
import com.adaptive_tutor_mobile.data.remote.dto.ResponseCourseFullViewDto
import com.adaptive_tutor_mobile.domain.model.courses.Chapter
import com.adaptive_tutor_mobile.domain.model.courses.CourseDetail
import com.adaptive_tutor_mobile.domain.model.courses.LessonSummary
import com.adaptive_tutor_mobile.domain.model.lesson.LessonWithContent
import com.adaptive_tutor_mobile.domain.repository.courses.CourseDetailRepository
import javax.inject.Inject

class CourseDetailRepositoryImpl @Inject constructor(
    private val api: CourseDetailApi
) : CourseDetailRepository {

    //------ Dev 5 ------
    private val lessonContentCache = mutableMapOf<String, LessonWithContent>()
    //-------------------
    override suspend fun getCourseFullView(courseId: String): Result<CourseDetail> {
        return runCatching {
            val dto = api.getCourseFullView(courseId)

            lessonContentCache.clear()

            dto.toDomain()
        }
    }

    //------ Dev 5 ------
    override fun getCachedLessonWithContent(lessonId: String): LessonWithContent? {
        return lessonContentCache[lessonId]
    }
    //-------------------

    // Mapping helpers

    private fun ResponseCourseFullViewDto.toDomain() =
        CourseDetail(
            id          = id,
            title       = title,
            description = description ?: "",
            visibility  = visibility,
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

    private fun LessonFullViewDTO.toDomain(): LessonSummary {
        val summary = LessonSummary(
            id      = id,
            title   = title,
            hasTest = testId != null
        )

        lessonContentCache[id] = LessonWithContent(
            summary = summary,
            contentMarkdown = contentMarkdown ?: ""
        )

        return summary
    }
}