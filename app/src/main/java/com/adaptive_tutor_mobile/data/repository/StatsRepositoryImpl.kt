package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.StatsApi
import com.adaptive_tutor_mobile.domain.model.AttemptSummary
import com.adaptive_tutor_mobile.domain.model.CourseStats
import com.adaptive_tutor_mobile.domain.repository.StatsRepository
import javax.inject.Inject

class StatsRepositoryImpl @Inject constructor(
    private val statsApi: StatsApi
) : StatsRepository {

    override suspend fun getCourseStats(courseId: String): Result<CourseStats> {
        return try {
            val response = statsApi.getCourseStats(courseId)
            if (response.isSuccessful) {
                val dto = response.body()
                if (dto != null) {
                    val courseStats = CourseStats(
                        courseTitle = dto.courseTitle,
                        totalTests = dto.totalTests,
                        totalTestDone = dto.totalTestDone,
                        passedTests = dto.passedTests,
                        bestScore = dto.bestScore.toFloat(),
                        lowestScore = dto.lowestScore.toFloat(),
                        avgScore = dto.avgScore.toFloat(),
                        hardestLessons = dto.hardestLessons.map { it.lessonTitle },
                        lastAttempts = dto.lastAttempts.map {
                            AttemptSummary(
                                attemptId = it.attemptId,
                                testId = it.testId,
                                testTitle = it.testTitle,
                                score = it.score.toFloat(),
                                scorePercent = it.scorePercent.toFloat(),
                                passed = it.passed,
                                attemptedAt = it.attemptedAt
                            )
                        }
                    )
                    Result.success(courseStats)
                }
 else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
