package com.adaptive_tutor_mobile.domain.usecase

import com.adaptive_tutor_mobile.domain.repository.ProgressRepository
import com.adaptive_tutor_mobile.ProgressTestFixtures.domainCourse
import com.adaptive_tutor_mobile.ProgressTestFixtures.progressDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressUseCasesTest {

    private val repo: ProgressRepository = mockk()

    @Test
    fun `GetEnrolledCoursesUseCase delegates to repository`() = runTest {
        val expected = listOf(domainCourse(courseId = "c1"), domainCourse(courseId = "c2"))
        coEvery { repo.getEnrolledCourses() } returns Result.success(expected)

        val result = GetEnrolledCoursesUseCase(repo).invoke()

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
        coVerify { repo.getEnrolledCourses() }
    }

    @Test
    fun `GetEnrolledCoursesUseCase propagates failures`() = runTest {
        coEvery { repo.getEnrolledCourses() } returns Result.failure(IllegalStateException("Boom"))

        val result = GetEnrolledCoursesUseCase(repo).invoke()

        assertTrue(result.isFailure)
        assertEquals("Boom", result.exceptionOrNull()?.message)
    }

    @Test
    fun `GetMyProgressUseCase passes courseId to repository`() = runTest {
        val expected = progressDto(progressPercent = 75.0)
        coEvery { repo.getMyProgress("course-42") } returns Result.success(expected)

        val result = GetMyProgressUseCase(repo).invoke("course-42")

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
        coVerify { repo.getMyProgress("course-42") }
    }
}