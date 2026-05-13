package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.LessonApi
import com.adaptive_tutor_mobile.domain.model.LessonDetail
import com.adaptive_tutor_mobile.domain.model.LessonResource
import com.adaptive_tutor_mobile.domain.repository.LessonRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class LessonRepositoryImpl @Inject constructor(
    private val api: LessonApi
) : LessonRepository {

    override suspend fun getLessonDetail(lessonId: String): Result<LessonDetail> = coroutineScope {
        try {
            val lesson = api.getLessonById(lessonId)
            val contentDeferred = async {
                try { api.getLessonContent(lessonId).string() } catch (exception: Exception) { "" }
            }

            val resourcesDeferred = async {
                try {
                    api.getResources(lessonId).map {
                        LessonResource(id = it.id, title = it.title, url = it.url)
                    }
                } catch (exception: Exception) { emptyList() }
            }

            val contentMarkdown = contentDeferred.await()
            val resources = resourcesDeferred.await()

            Result.success(
                LessonDetail(
                    id = lesson.id,
                    title = lesson.title,
                    contentMarkdown = contentMarkdown,
                    resources = resources
                )
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}