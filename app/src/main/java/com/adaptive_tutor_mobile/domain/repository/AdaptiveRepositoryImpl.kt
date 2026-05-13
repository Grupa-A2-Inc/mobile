package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.AdaptiveApi
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveExerciseStudentDto
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveStartRequestDto
import com.adaptive_tutor_mobile.data.remote.dto.OptionForStudentDto
import com.adaptive_tutor_mobile.data.remote.dto.QuestionForStudentDto
import com.adaptive_tutor_mobile.domain.model.AdaptiveSession
import com.adaptive_tutor_mobile.domain.repository.AdaptiveRepository
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
                    ex.toQuestionForStudent(listIndex)
                }
            )
        } else {
            error(parseError(response.code(), response.errorBody()?.string()))
        }
    }

    private fun AdaptiveExerciseStudentDto.toQuestionForStudent(listIndex: Int): QuestionForStudentDto {
        // Resolve questionId: prefer explicit int, fall back to parsing the string id, then list index
        val resolvedId = questionId?.toIntOrNull() ?: listIndex

        // If server returned options with int IDs, use them directly.
        // Otherwise convert plain-string answers to synthetic options (index = optionId).
        val resolvedOptions = options ?: answers?.mapIndexed { i, text ->
            OptionForStudentDto(optionId = i, text = text, displayOrder = i)
        }

        return QuestionForStudentDto(
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
