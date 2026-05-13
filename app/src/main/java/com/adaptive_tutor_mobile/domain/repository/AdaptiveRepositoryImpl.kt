package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.AdaptiveApi
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveAnswerDto
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveStartRequestDto
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveSubmitRequestDto
import com.adaptive_tutor_mobile.domain.model.AdaptiveAnswer
import com.adaptive_tutor_mobile.domain.model.AdaptiveExercise
import com.adaptive_tutor_mobile.domain.model.AdaptiveResult
import com.adaptive_tutor_mobile.domain.model.AdaptiveSession
import com.adaptive_tutor_mobile.domain.model.ExerciseResult
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
            AdaptiveStartRequestDto(
                subjectId = subjectId,
                topicId = topicId,
                count = count
            )
        )

        if (response.isSuccessful) {
            val body = response.body() ?: error("Empty response body")

            AdaptiveSession(
                sessionId = body.sessionId,
                expiresAt = body.expiresAt,
                exercises = body.exercises.map { dto ->
                    AdaptiveExercise(
                        id = dto.exerciseId,
                        text = dto.text,
                        type = dto.type,
                        answers = dto.answers,
                        difficulty = dto.difficulty
                    )
                }
            )
        } else {
            error(parseError(response.code(), response.errorBody()?.string()))
        }
    }

    override suspend fun submitSession(
        sessionId: String,
        answers: List<AdaptiveAnswer>
    ): Result<AdaptiveResult> = runCatching {
        val response = api.submitSession(
            sessionId = sessionId,
            request = AdaptiveSubmitRequestDto(
                answers = answers.map { answer ->
                    AdaptiveAnswerDto(
                        exerciseId = answer.exerciseId,
                        givenAnswers = answer.givenAnswers,
                        timeSpent = answer.timeSpent
                    )
                }
            )
        )

        if (response.isSuccessful) {
            val body = response.body() ?: error("Empty response body")

            AdaptiveResult(
                totalScore = body.totalScore,
                results = body.results.map { dto ->
                    ExerciseResult(
                        exerciseId = dto.exerciseId,
                        correct = dto.correct,
                        score = dto.score,
                        correctAnswers = dto.correctAnswers,
                        givenAnswers = dto.givenAnswers
                    )
                }
            )
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
            } catch (_: Exception) {
                "Eroare $code"
            }
        }

        return when (code) {
            400 -> "Date invalide pentru sesiunea adaptivă"
            401 -> "Nu ești autentificat"
            404 -> "Sesiunea adaptivă nu a fost găsită"
            else -> "Eroare $code"
        }
    }
}