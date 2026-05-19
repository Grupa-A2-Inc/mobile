package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.AttemptHistoryApi
import com.adaptive_tutor_mobile.data.remote.dto.AttemptStatusDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AttemptHistoryRepositoryImplTest {

    private val api = mockk<AttemptHistoryApi>()
    private val repository = AttemptHistoryRepositoryImpl(api)

    @Test
    fun `getAttempts should return success when api returns list`() = runTest {
        // Arrange
        val testId = "test-1"
        val dtoList = listOf(
            AttemptStatusDto("a1", 1, 8.0, 80.0, true, "2023-01-01", "PASSED")
        )
        coEvery { api.getMyAttempts(testId) } returns Response.success(dtoList)

        // Act
        val result = repository.getAttempts(testId)

        // Assert
        assertTrue(result.isSuccess)
        val attempts = result.getOrNull()!!
        assertEquals(1, attempts.size)
        assertEquals("a1", attempts[0].attemptId)
        assertEquals(8.0, attempts[0].score, 0.0)
    }

    @Test
    fun `getAttempts should return failure when api returns error`() = runTest {
        // Arrange
        val testId = "test-1"
        coEvery { api.getMyAttempts(testId) } returns Response.error(500, "".toResponseBody())

        // Act
        val result = repository.getAttempts(testId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Error: 500", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getBestAttempt should return success when api returns dto`() = runTest {
        // Arrange
        val testId = "test-1"
        val dto = AttemptStatusDto("a2", 2, 9.5, 95.0, true, "2023-01-02", "PASSED")
        coEvery { api.getMyBestAttempt(testId) } returns Response.success(dto)

        // Act
        val result = repository.getBestAttempt(testId)

        // Assert
        assertTrue(result.isSuccess)
        val best = result.getOrNull()!!
        assertEquals("a2", best.attemptId)
        assertEquals(9.5, best.score, 0.0)
    }

    @Test
    fun `getBestAttempt should return failure when api returns null body`() = runTest {
        // Arrange
        val testId = "test-1"
        coEvery { api.getMyBestAttempt(testId) } returns Response.success(null)

        // Act
        val result = repository.getBestAttempt(testId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("No best attempt found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getBestAttempt should return failure when api returns error`() = runTest {
        // Arrange
        val testId = "test-1"
        coEvery { api.getMyBestAttempt(testId) } returns Response.error(401, "".toResponseBody())

        // Act
        val result = repository.getBestAttempt(testId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Error: 401", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getBestAttempt should return failure when api throws exception`() = runTest {
        // Arrange
        val testId = "test-1"
        val exception = RuntimeException("Connection Lost")
        coEvery { api.getMyBestAttempt(testId) } throws exception

        // Act
        val result = repository.getBestAttempt(testId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
