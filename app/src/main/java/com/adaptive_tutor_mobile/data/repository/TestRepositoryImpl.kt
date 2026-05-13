package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.TestApi
import com.adaptive_tutor_mobile.data.remote.dto.SubmitAnswersRequest
import com.adaptive_tutor_mobile.domain.model.*
import com.adaptive_tutor_mobile.data.remote.dto.AttemptReportDTO
import com.adaptive_tutor_mobile.data.remote.dto.StartAttemptResponseDto
import com.adaptive_tutor_mobile.data.remote.dto.SubmitRequestDto
import com.adaptive_tutor_mobile.domain.repository.TestRepository
import javax.inject.Inject

class TestRepositoryImpl @Inject constructor(
    private val api: TestApi
) : TestRepository {

    override suspend fun startAttempt(testId: String): Result<TestAttempt> {
        return try {
            val dto = api.startAttempt(testId)
            Result.success(
                TestAttempt(
                    attemptId = dto.attemptId,
                    timeLimitSec = dto.timeLimitSec,
                    questions = dto.questions.map { q ->
                        Question(
                            id = q.id,
                            type = QuestionType.valueOf(q.type),
                            content = q.content,
                            options = q.options.map { Option(it.id, it.text) }
                        )
                    }
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitAttempt(attemptId: String, answers: Map<Int, List<Int>>): Result<Unit> {
        return try {
            api.submitAttempt(attemptId, SubmitAnswersRequest(answers))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAttemptResult(attemptId: String): Result<AttemptResult> {
        return try {
            val dto = api.getAttemptResult(attemptId)
            Result.success(
                AttemptResult(
                    score = dto.score,
                    scorePercent = dto.scorePercent,
                    passed = dto.passed,
                    questions = dto.questions.map { qr ->
                        QuestionResult(
                            question = Question(
                                id = qr.question.id,
                                type = QuestionType.valueOf(qr.question.type),
                                content = qr.question.content,
                                options = qr.question.options.map { Option(it.id, it.text) }
                            ),
                            correctOptionIds = qr.correctOptionIds,
                            userOptionIds = qr.userOptionIds
                        )
                    }
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
    override suspend fun startTest(testId: String): Result<StartAttemptResponseDto> = try {
        Result.success(api.startTest(testId))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun submitAttempt(
        attemptId: String,
        request: SubmitRequestDto
    ): Result<AttemptReportDTO> = try {
        Result.success(api.submitAttempt(attemptId, request))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
