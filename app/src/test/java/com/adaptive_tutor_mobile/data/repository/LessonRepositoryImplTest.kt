package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.LessonApi
import com.adaptive_tutor_mobile.data.remote.dto.ResponseLessonResourceDto
import com.adaptive_tutor_mobile.data.remote.dto.TestEntityDto
import com.adaptive_tutor_mobile.domain.model.LessonSummary
import com.adaptive_tutor_mobile.domain.model.LessonWithContent
import com.adaptive_tutor_mobile.domain.repository.CourseDetailRepository
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response

class LessonRepositoryImplTest {

    private val courseDetailRepository: CourseDetailRepository = mock()
    private val api: LessonApi = mock()
    private val repository = LessonRepositoryImpl(courseDetailRepository, api)

    @Test
    fun `getLessonDetail returns failure when cache missing`() = runTest {
        whenever(courseDetailRepository.getCachedLessonWithContent("l1")).thenReturn(null)

        val result = repository.getLessonDetail("l1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `getLessonDetail maps cached lesson and resources`() = runTest {
        val cached = LessonWithContent(
            summary = LessonSummary(id = "l1", title = "Lesson 1", hasTest = true),
            contentMarkdown = "# Markdown"
        )
        whenever(courseDetailRepository.getCachedLessonWithContent("l1")).thenReturn(cached)
        whenever(api.getResources("l1")).thenReturn(
            listOf(ResponseLessonResourceDto(id = "r1", lessonId = "l1", title = "Res", url = "https://x"))
        )

        val result = repository.getLessonDetail("l1")

        assertTrue(result.isSuccess)
        val detail = result.getOrThrow()
        assertEquals("l1", detail.id)
        assertEquals("Lesson 1", detail.title)
        assertEquals(1, detail.resources.size)
        assertEquals("Res", detail.resources.first().title)
    }

    @Test
    fun `getLessonDetail uses empty resources when api fails`() = runTest {
        val cached = LessonWithContent(
            summary = LessonSummary(id = "l1", title = "Lesson 1", hasTest = false),
            contentMarkdown = "# Markdown"
        )
        whenever(courseDetailRepository.getCachedLessonWithContent("l1")).thenReturn(cached)
        whenever(api.getResources("l1")).thenThrow(RuntimeException("boom"))

        val result = repository.getLessonDetail("l1")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().resources.isEmpty())
    }

    @Test
    fun `checkLessonTest returns test id when response is successful`() = runTest {
        val testEntity = TestEntityDto(
            id = "t1",
            lessonId = "l1",
            createdBy = "u1",
            title = "Test",
            description = null,
            timeLimitSec = 60,
            status = "PUBLISHED",
            aiEnabled = false,
            createdAt = null,
            updatedAt = null
        )
        whenever(api.getLessonTest("l1")).thenReturn(Response.success(testEntity))

        val result = repository.checkLessonTest("l1")

        assertEquals("t1", result)
    }

    @Test
    fun `checkLessonTest returns null on error response`() = runTest {
        val body = "error".toResponseBody("text/plain".toMediaType())
        whenever(api.getLessonTest("l1")).thenReturn(Response.error(404, body))

        val result = repository.checkLessonTest("l1")

        assertEquals(null, result)
    }

    @Test
    fun `checkLessonTest returns null when api throws`() = runTest {
        whenever(api.getLessonTest("l1")).thenThrow(RuntimeException("network error"))

        val result = repository.checkLessonTest("l1")

        assertEquals(null, result)
    }

    @Test
    fun `markVisited silently swallows api exceptions`() = runTest {
        whenever(api.getLessonById("l1")).thenThrow(RuntimeException("offline"))

        // should not throw
        repository.markVisited("l1")
    }
}
