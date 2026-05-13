package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.LessonApi
import com.adaptive_tutor_mobile.domain.model.LessonDetail
import com.adaptive_tutor_mobile.domain.model.LessonResource
import com.adaptive_tutor_mobile.domain.repository.CourseDetailRepository
import com.adaptive_tutor_mobile.domain.repository.LessonRepository
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class LessonRepositoryImpl @Inject constructor(
    private val courseDetailRepository: CourseDetailRepository,
    private val api: LessonApi
) : LessonRepository {

    override suspend fun getLessonDetail(lessonId: String): Result<LessonDetail> = coroutineScope {
        try {
            // 1. Get the heavy content from the dictionary cache
            val cachedLesson = courseDetailRepository.getCachedLessonWithContent(lessonId)
                ?: throw Exception("Lecția nu a fost găsită în cache. Te rugăm să reîncarci cursul.")

            // 2. Fetch resources if needed
            val resources = try {
                api.getResources(lessonId).map {
                    LessonResource(id = it.id, title = it.title, url = it.url)
                }
            } catch (e: Exception) {
                emptyList()
            }

            // 3. Combine them into the final UI state
            Result.success(
                LessonDetail(
                    id = cachedLesson.summary.id,
                    title = cachedLesson.summary.title,
                    contentMarkdown = cachedLesson.contentMarkdown,
                    resources = resources
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}