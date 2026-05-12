package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.CourseApi
import com.adaptive_tutor_mobile.data.remote.api.ProgressApi
import com.adaptive_tutor_mobile.data.remote.dto.ProgressWithLessonListDto
import com.adaptive_tutor_mobile.domain.model.EnrolledCourse
import com.adaptive_tutor_mobile.domain.model.toDomain
import com.adaptive_tutor_mobile.domain.repository.ProgressRepository
import com.google.gson.JsonParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val courseApi: CourseApi,
    private val progressApi: ProgressApi
) : ProgressRepository {

    override suspend fun getEnrolledCourses(): Result<List<EnrolledCourse>> = runCatching {
        val response = progressApi.getMyEnrolledCourses()
        if (response.isSuccessful) {
            response.body()?.content?.map { it.toDomain() } ?: emptyList()
        } else {
            error(parseError(response.code(), response.errorBody()?.string()))
        }
    }

    override suspend fun getCompletedCourses(): Result<List<EnrolledCourse>> = runCatching {
        val response = progressApi.getCompletedCourses()
        if (response.isSuccessful) {
            response.body()?.map { it.toDomain() } ?: emptyList()
        } else {
            error(parseError(response.code(), response.errorBody()?.string()))
        }
    }

    override suspend fun getMyProgress(courseId: String): Result<ProgressWithLessonListDto> = runCatching {
        val response = progressApi.getMyProgress(courseId)
        if (response.isSuccessful) {
            response.body() ?: error("Răspuns gol")
        } else {
            error(parseError(response.code(), response.errorBody()?.string()))
        }
    }

    private fun parseError(code: Int, body: String?): String {
        if (!body.isNullOrBlank()) {
            return try {
                val json = JsonParser.parseString(body).asJsonObject
                json.get("message")?.asString
                    ?: json.get("error")?.asString
                    ?: "Eroare $code"
            } catch (_: Exception) { "Eroare $code" }
        }
        return when (code) {
            401 -> "Sesiune expirată"
            403 -> "Nu ai permisiunea de a accesa această resursă"
            404 -> "Resursa nu a fost găsită"
            else -> "Eroare $code"
        }
    }
}