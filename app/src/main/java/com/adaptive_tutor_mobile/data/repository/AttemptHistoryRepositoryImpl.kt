package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.AttemptHistoryApi
import com.adaptive_tutor_mobile.domain.model.AttemptHistory
import com.adaptive_tutor_mobile.domain.model.BestAttempt
import com.adaptive_tutor_mobile.domain.repository.AttemptHistoryRepository
import javax.inject.Inject

class AttemptHistoryRepositoryImpl @Inject constructor(
    private val api: AttemptHistoryApi
) : AttemptHistoryRepository {

    override suspend fun getAttempts(testId: String): Result<List<AttemptHistory>> {
        return try {
            val response = api.getMyAttempts(testId)
            if (response.isSuccessful) {
                val attempts = response.body()?.map { dto ->
                    AttemptHistory(
                        attemptId = dto.attemptId,
                        attemptNumber = dto.attemptNumber,
                        score = dto.score,
                        scorePercent = dto.scorePercent,
                        passed = dto.passed,
                        date = dto.startedAt,
                        status = dto.status
                    )
                } ?: emptyList()
                Result.success(attempts)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBestAttempt(testId: String): Result<BestAttempt> {
        return try {
            val response = api.getMyBestAttempt(testId)
            if (response.isSuccessful) {
                val dto = response.body()
                if (dto != null) {
                    Result.success(
                        BestAttempt(
                            attemptId = dto.attemptId,
                            score = dto.score,
                            scorePercent = dto.scorePercent,
                            date = dto.startedAt
                        )
                    )
                } else {
                    Result.failure(Exception("No best attempt found"))
                }
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
