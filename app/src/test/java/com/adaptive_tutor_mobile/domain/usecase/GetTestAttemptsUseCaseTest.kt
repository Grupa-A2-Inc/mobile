package com.adaptive_tutor_mobile.domain.usecase

import com.adaptive_tutor_mobile.domain.model.AttemptHistory
import com.adaptive_tutor_mobile.domain.model.BestAttempt
import com.adaptive_tutor_mobile.domain.repository.AttemptHistoryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetTestAttemptsUseCaseTest {

    private val repository = mockk<AttemptHistoryRepository>()
    private val useCase = GetTestAttemptsUseCase(repository)

    @Test
    fun `invoke should return success when repository returns attempts and best attempt`() = runTest {
        // Arrange
        val testId = "test-123"
        val attempts = listOf(
            AttemptHistory("1", 1, 8.5, 85.0, true, "2023-01-01", "PASSED"),
            AttemptHistory("2", 2, 9.0, 90.0, true, "2023-01-02", "PASSED")
        )
        val bestAttempt = BestAttempt("2", 9.0, 90.0, "2023-01-02")
        
        coEvery { repository.getAttempts(testId) } returns Result.success(attempts)
        coEvery { repository.getBestAttempt(testId) } returns Result.success(bestAttempt)

        // Act
        val result = useCase(testId)

        // Assert
        assertTrue(result.isSuccess)
        val data = result.getOrNull()
        assertEquals(attempts, data?.attempts)
        assertEquals(bestAttempt, data?.bestAttempt)
    }

    @Test
    fun `invoke should return success with null bestAttempt when getBestAttempt fails`() = runTest {
        // Arrange
        val testId = "test-123"
        val attempts = listOf(
            AttemptHistory("1", 1, 8.5, 85.0, true, "2023-01-01", "PASSED")
        )
        
        coEvery { repository.getAttempts(testId) } returns Result.success(attempts)
        coEvery { repository.getBestAttempt(testId) } returns Result.failure(Exception("Not found"))

        // Act
        val result = useCase(testId)

        // Assert
        assertTrue(result.isSuccess)
        val data = result.getOrNull()
        assertEquals(attempts, data?.attempts)
        assertEquals(null, data?.bestAttempt)
    }

    @Test
    fun `invoke should return failure when getAttempts fails`() = runTest {
        // Arrange
        val testId = "test-123"
        val expectedException = Exception("Network Error")
        
        coEvery { repository.getAttempts(testId) } returns Result.failure(expectedException)
        coEvery { repository.getBestAttempt(testId) } returns Result.failure(Exception("Irrelevant"))

        // Act
        val result = useCase(testId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
    }

    @Test
    fun `invoke should return failure when repository throws exception`() = runTest {
        // Arrange
        val testId = "test-123"
        val expectedException = RuntimeException("Unexpected Crash")
        
        coEvery { repository.getAttempts(testId) } throws expectedException

        // Act
        val result = useCase(testId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
    }
}
