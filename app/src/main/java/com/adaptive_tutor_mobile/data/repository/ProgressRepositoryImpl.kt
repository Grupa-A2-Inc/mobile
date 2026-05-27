package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.CourseApi
import com.adaptive_tutor_mobile.data.remote.api.EnrollmentApi
import com.adaptive_tutor_mobile.data.remote.api.ProgressApi
import com.adaptive_tutor_mobile.data.remote.dto.ProgressWithLessonListDto
import com.adaptive_tutor_mobile.domain.model.courses.EnrolledCourse
import com.adaptive_tutor_mobile.domain.model.courses.toDomain
import com.adaptive_tutor_mobile.domain.repository.stats.ProgressRepository
import com.google.gson.JsonParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val courseApi: CourseApi,
    private val progressApi: ProgressApi,
    private val enrollmentApi: EnrollmentApi
) : ProgressRepository {

    override suspend fun getEnrolledCourses(): Result<List<EnrolledCourse>> = runCatching {
        // Fetch enrolled courses and public course IDs in parallel.
        // Cursurile PUBLIC pot fi parasitede student; cele PRIVATE (asignate de profesor) nu.
        val enrolledResponse = progressApi.getMyEnrolledCourses()

        // Fetch all public courses (large page) to build a set of public IDs.
        val publicIds: Set<String> = try {
            val publicResponse = enrollmentApi.getPublicCourses(page = 0, size = 500)
            if (publicResponse.isSuccessful) {
                publicResponse.body()?.content?.map { it.id }?.toSet() ?: emptySet()
            } else {
                emptySet()
            }
        } catch (_: Exception) {
            emptySet()  // Dacă request-ul eșuează, nu blocam ecranul
        }

        if (enrolledResponse.isSuccessful) {
            enrolledResponse.body()?.content?.map { dto ->
                dto.toDomain().copy(canUnenroll = dto.courseId in publicIds)
            } ?: emptyList()
        } else {
            error(parseError(enrolledResponse.code(), enrolledResponse.errorBody()?.string()))
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
                json["message"]?.asString
                    ?: json["error"]?.asString
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
