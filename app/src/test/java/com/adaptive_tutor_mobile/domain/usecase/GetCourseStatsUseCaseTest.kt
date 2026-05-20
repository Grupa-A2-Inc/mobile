package com.adaptive_tutor_mobile.domain.usecase

import com.adaptive_tutor_mobile.domain.model.stats.CourseStats
import com.adaptive_tutor_mobile.domain.repository.stats.StatsRepository
import com.adaptive_tutor_mobile.domain.usecase.stats.GetCourseStatsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetCourseStatsUseCaseTest {

    private val repository = mockk<StatsRepository>()
    private val useCase = GetCourseStatsUseCase(repository)

    @Test
    fun `invoke should return success when repository returns success`() = runTest {
        // Arrange
        val courseId = "course-123"
        val expectedStats = CourseStats(
            courseTitle = "Kotlin Advanced",
            totalTests = 10,
            totalTestDone = 5,
            passedTests = 4,
            bestScore = 95f,
            lowestScore = 60f,
            avgScore = 80f,
            hardestLessons = listOf("Coroutines", "Flow"),
            lastAttempts = emptyList()
        )
        coEvery { repository.getCourseStats(courseId) } returns Result.success(expectedStats)

        // Act
        val result = useCase(courseId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedStats, result.getOrNull())
    }

    @Test
    fun `invoke should return failure when repository returns failure`() = runTest {
        // Arrange
        val courseId = "course-123"
        val expectedException = Exception("Network Error")
        coEvery { repository.getCourseStats(courseId) } returns Result.failure(expectedException)

        // Act
        val result = useCase(courseId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
    }
}
