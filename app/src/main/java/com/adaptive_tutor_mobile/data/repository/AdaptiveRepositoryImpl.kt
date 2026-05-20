package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.AdaptiveApi
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveAttemptReportDTO
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveExerciseStudentDto
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveQuestionForStudentDto
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveStartRequestDto
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveSubmitRequestDto
import com.adaptive_tutor_mobile.data.remote.dto.OptionForStudentDto
import com.adaptive_tutor_mobile.domain.model.adaptive.AdaptiveSession
import com.adaptive_tutor_mobile.domain.repository.adaptive.AdaptiveRepository
import com.google.gson.JsonParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdaptiveRepositoryImpl @Inject constructor(
    private val api: AdaptiveApi
) : AdaptiveRepository {

    override suspend fun startSession(
        subjectId: Int,
        topicId: Int,
        count: Int
    ): Result<AdaptiveSession> = runCatching {
        val response = api.startSession(
            AdaptiveStartRequestDto(subjectId = subjectId, topicId = topicId, count = count)
        )

        if (response.isSuccessful) {
            val body = response.body() ?: error("Empty response body")
            AdaptiveSession(
                sessionId = body.sessionId,
                attemptId = body.attemptId,
                expiresAt = body.expiresAt,
                questions = body.exercises.orEmpty().mapIndexed { listIndex, ex ->
                    ex.toAdaptiveQuestionForStudent(listIndex)
                }
            )
        } else {
            error(parseError(response.code(), response.errorBody()?.string()))
        }
    }

    override suspend fun submitSession(
        sessionId: String,
        request: AdaptiveSubmitRequestDto
    ): Result<AdaptiveAttemptReportDTO> = runCatching {
        val response = api.submitSession(sessionId, request)
        if (response.isSuccessful) {
            val body = response.body() ?: error("Empty response body")

            val questions = body.questions.orEmpty()
            val correctCount = questions.count { it.correct }
            val totalCount = questions.size

            val calculatedPercent = if (totalCount > 0) {
                (correctCount.toDouble() / totalCount.toDouble()) * 100.0
            } else {
                0.0
            }

            val finalPercent = when {
                body.scorePercent != null && body.scorePercent > 1.0 -> body.scorePercent
                body.scorePercent != null -> body.scorePercent * 100.0
                else -> calculatedPercent
            }

            val finalPassed = body.passed ?: (finalPercent >= 50.0)

            body.copy(
                scorePercent = finalPercent,
                passed = finalPassed
            )
        } else {
            error(parseError(response.code(), response.errorBody()?.string()))
        }
    }

    private fun AdaptiveExerciseStudentDto.toAdaptiveQuestionForStudent(listIndex: Int): AdaptiveQuestionForStudentDto {
        val resolvedId = questionId ?: listIndex.toString()

        val resolvedOptions = options ?: answers?.mapIndexed { i, text ->
            OptionForStudentDto(optionId = i, text = text, displayOrder = i)
        }

        return AdaptiveQuestionForStudentDto(
            questionId = resolvedId,
            questionType = questionType,
            content = content,
            difficulty = difficulty,
            options = resolvedOptions
        )
    }

    private fun parseError(code: Int, body: String?): String {
        if (!body.isNullOrBlank()) {
            return try {
                val json = JsonParser.parseString(body).asJsonObject
                json["message"]?.asString ?: json["error"]?.asString ?: "Eroare $code"
            } catch (_: Exception) { "Eroare $code" }
        }
        return when (code) {
            400 -> "Date invalide pentru sesiunea adaptivă"
            401 -> "Nu ești autentificat"
            404 -> "Sesiunea adaptivă nu a fost găsită"
            else -> "Eroare $code"
        }
    }
}
