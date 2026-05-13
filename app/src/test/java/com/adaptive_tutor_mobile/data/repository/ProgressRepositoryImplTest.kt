package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.CourseApi
import com.adaptive_tutor_mobile.data.remote.api.ProgressApi
import com.adaptive_tutor_mobile.ProgressTestFixtures.completedDto
import com.adaptive_tutor_mobile.ProgressTestFixtures.enrolledDto
import com.adaptive_tutor_mobile.ProgressTestFixtures.progressDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import com.adaptive_tutor_mobile.data.remote.dto.PageDto

class ProgressRepositoryImplTest {

    private lateinit var courseApi: CourseApi
    private lateinit var progressApi: ProgressApi
    private lateinit var repository: ProgressRepositoryImpl

    @Before
    fun setup() {
        courseApi = mockk()
        progressApi = mockk()
        repository = ProgressRepositoryImpl(courseApi, progressApi)
    }

    // ── getEnrolledCourses ───────────────────────────────────────────────────

    @Test
    fun `getEnrolledCourses success maps DTOs to domain list`() = runTest {
        coEvery { progressApi.getMyEnrolledCourses() } returns Response.success(
            PageDto(
                content = listOf(
                    enrolledDto(courseId = "c1", title = "Math"),
                    enrolledDto(courseId = "c2", title = "Physics", progress = 88.0)
                )
            )
        )

        val result = repository.getEnrolledCourses()

        assertTrue(result.isSuccess)
        val courses = result.getOrNull()!!
        assertEquals(2, courses.size)
        assertEquals("c1", courses[0].courseId)
        assertEquals("Math", courses[0].courseTitle)
        assertEquals(88.0, courses[1].progressPercent, 0.001)
    }

    @Test
    fun `getEnrolledCourses returns empty list when body is null`() = runTest {
        coEvery { progressApi.getMyEnrolledCourses() } returns Response.success(null)

        val result = repository.getEnrolledCourses()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `getEnrolledCourses returns empty list when backend returns empty array`() = runTest {
        coEvery { progressApi.getMyEnrolledCourses() } returns Response.success(
            PageDto(content = emptyList())
        )

        val result = repository.getEnrolledCourses()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `getEnrolledCourses parses message field from json error body`() = runTest {
        val body = """{"timestamp":"2025-01-01","status":403,"message":"Forbidden"}"""
            .toResponseBody("application/json".toMediaType())
        coEvery { progressApi.getMyEnrolledCourses() } returns Response.error(403, body)

        val result = repository.getEnrolledCourses()

        assertTrue(result.isFailure)
        assertEquals("Forbidden", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getEnrolledCourses falls back to 401 default when body empty`() = runTest {
        coEvery { progressApi.getMyEnrolledCourses() } returns Response.error(
            401, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.getEnrolledCourses()

        assertTrue(result.isFailure)
        assertEquals("Sesiune expirată", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getEnrolledCourses falls back to 404 default when body empty`() = runTest {
        coEvery { progressApi.getMyEnrolledCourses() } returns Response.error(
            404, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.getEnrolledCourses()

        assertTrue(result.isFailure)
        assertEquals("Resursa nu a fost găsită", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getEnrolledCourses falls back to generic code when body malformed`() = runTest {
        coEvery { progressApi.getMyEnrolledCourses() } returns Response.error(
            500,
            "not a json".toResponseBody("application/json".toMediaType())
        )

        val result = repository.getEnrolledCourses()

        assertTrue(result.isFailure)
        assertEquals("Eroare 500", result.exceptionOrNull()?.message)
    }

    // ── getCompletedCourses ──────────────────────────────────────────────────

    @Test
    fun `getCompletedCourses maps CompletedCourseDto with 100 percent`() = runTest {
        coEvery { progressApi.getCompletedCourses() } returns Response.success(
            listOf(completedDto(courseId = "c9", title = "Done Course"))
        )

        val result = repository.getCompletedCourses()

        assertTrue(result.isSuccess)
        val courses = result.getOrNull()!!
        assertEquals(1, courses.size)
        assertEquals("c9", courses[0].courseId)
        assertEquals(100.0, courses[0].progressPercent, 0.001)
        assertTrue(courses[0].isCompleted)
    }

    @Test
    fun `getCompletedCourses returns failure on error`() = runTest {
        coEvery { progressApi.getCompletedCourses() } returns Response.error(
            403,
            """{"message":"Forbidden"}""".toResponseBody("application/json".toMediaType())
        )

        val result = repository.getCompletedCourses()

        assertTrue(result.isFailure)
        assertEquals("Forbidden", result.exceptionOrNull()?.message)
    }

    // ── getMyProgress ────────────────────────────────────────────────────────

    @Test
    fun `getMyProgress returns ProgressWithLessonListDto when successful`() = runTest {
        val dto = progressDto(totalLessons = 5, visitedLessons = 3, progressPercent = 60.0)
        coEvery { progressApi.getMyProgress("course-1") } returns Response.success(dto)

        val result = repository.getMyProgress("course-1")

        assertTrue(result.isSuccess)
        val progress = result.getOrNull()!!
        assertEquals(5, progress.totalLessons)
        assertEquals(3, progress.visitedLessons)
        assertEquals(60.0, progress.progressPercent, 0.001)
    }

    @Test
    fun `getMyProgress null body returns failure`() = runTest {
        coEvery { progressApi.getMyProgress(any()) } returns Response.success(null)

        val result = repository.getMyProgress("course-1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `getMyProgress 404 falls back to default message`() = runTest {
        coEvery { progressApi.getMyProgress(any()) } returns Response.error(
            404, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.getMyProgress("nope")

        assertTrue(result.isFailure)
        assertEquals("Resursa nu a fost găsită", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMyProgress parses error field when message absent`() = runTest {
        val body = """{"error":"Forbidden resource"}"""
            .toResponseBody("application/json".toMediaType())
        coEvery { progressApi.getMyProgress(any()) } returns Response.error(403, body)

        val result = repository.getMyProgress("course-1")

        assertTrue(result.isFailure)
        assertEquals("Forbidden resource", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMyProgress malformed json falls back to code`() = runTest {
        coEvery { progressApi.getMyProgress(any()) } returns Response.error(
            500, "not-json".toResponseBody("application/json".toMediaType())
        )

        val result = repository.getMyProgress("course-1")

        assertTrue(result.isFailure)
        assertEquals("Eroare 500", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMyProgress network exception returns failure`() = runTest {
        coEvery { progressApi.getMyProgress(any()) } throws RuntimeException("timeout")

        val result = repository.getMyProgress("course-1")

        assertTrue(result.isFailure)
        assertEquals("timeout", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCompletedCourses returns empty list when body is null`() = runTest {
        coEvery { progressApi.getCompletedCourses() } returns Response.success(null)

        val result = repository.getCompletedCourses()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `getCompletedCourses 401 uses default message`() = runTest {
        coEvery { progressApi.getCompletedCourses() } returns Response.error(
            401, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.getCompletedCourses()

        assertTrue(result.isFailure)
        assertEquals("Sesiune expirată", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCompletedCourses network exception returns failure`() = runTest {
        coEvery { progressApi.getCompletedCourses() } throws RuntimeException("no network")

        val result = repository.getCompletedCourses()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getEnrolledCourses 403 uses default message`() = runTest {
        coEvery { progressApi.getMyEnrolledCourses() } returns Response.error(
            403, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.getEnrolledCourses()

        assertTrue(result.isFailure)
        assertEquals("Nu ai permisiunea de a accesa această resursă", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getEnrolledCourses network exception returns failure`() = runTest {
        coEvery { progressApi.getMyEnrolledCourses() } throws RuntimeException("offline")

        val result = repository.getEnrolledCourses()

        assertTrue(result.isFailure)
    }
}