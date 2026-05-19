package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.StatsApi
import com.adaptive_tutor_mobile.data.remote.dto.AttemptDetailsDto
import com.adaptive_tutor_mobile.data.remote.dto.CourseStatsDto
import com.adaptive_tutor_mobile.data.remote.dto.DifficultyLessonDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class StatsRepositoryImplTest {

    private val api = mockk<StatsApi>()
    private val repository = StatsRepositoryImpl(api)

    @Test
    fun `getCourseStats should return success when api returns success`() = runTest {
        // Arrange
        val courseId = "course-1"
        val dto = CourseStatsDto(
            courseTitle = "Kotlin",
            totalTests = 10,
            totalTestDone = 5,
            passedTests = 4,
            bestScore = 10.0,
            lowestScore = 5.0,
            avgScore = 7.5,
            hardestLessons = listOf(
                DifficultyLessonDto("l1", "Lesson 1", 5.0, 7.0, 2.0)
            ),
            lastAttempts = listOf(
                AttemptDetailsDto("a1", "t1", "Test 1", 9.0, 90.0, true, "2023-01-01")
            )
        )
        coEvery { api.getCourseStats(courseId) } returns Response.success(dto)

        // Act
        val result = repository.getCourseStats(courseId)

        // Assert
        assertTrue(result.isSuccess)
        val stats = result.getOrNull()!!
        assertEquals("Kotlin", stats.courseTitle)
        assertEquals(1, stats.hardestLessons.size)
        assertEquals("Lesson 1", stats.hardestLessons[0])
        assertEquals(1, stats.lastAttempts.size)
        assertEquals("Test 1", stats.lastAttempts[0].testTitle)
        assertEquals(9.0f, stats.lastAttempts[0].score)
    }

    @Test
    fun `getCourseStats should return failure when api returns error`() = runTest {
        // Arrange
        val courseId = "course-1"
        coEvery { api.getCourseStats(courseId) } returns Response.error(404, "".toResponseBody())

        // Act
        val result = repository.getCourseStats(courseId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Error: 404", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCourseStats should return failure when api returns null body`() = runTest {
        // Arrange
        val courseId = "course-1"
        coEvery { api.getCourseStats(courseId) } returns Response.success(null)

        // Act
        val result = repository.getCourseStats(courseId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Response body is null", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCourseStats should return failure when api throws exception`() = runTest {
        // Arrange
        val courseId = "course-1"
        val exception = RuntimeException("Network Error")
        coEvery { api.getCourseStats(courseId) } throws exception

        // Act
        val result = repository.getCourseStats(courseId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
