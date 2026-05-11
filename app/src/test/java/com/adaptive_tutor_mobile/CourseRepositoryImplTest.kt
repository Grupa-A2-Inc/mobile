package com.adaptive_tutor_mobile

import com.adaptive_tutor_mobile.data.remote.api.EnrollmentApi
import com.adaptive_tutor_mobile.data.remote.dto.PageResponseCourseDto
import com.adaptive_tutor_mobile.data.remote.dto.ResponseCourseDto
import com.adaptive_tutor_mobile.data.repository.CourseRepositoryImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response

class CourseRepositoryImplTest {

    private val enrollmentApi: EnrollmentApi = mock()
    private val repository = CourseRepositoryImpl(enrollmentApi)

    @Test
    fun `getPublicCourses returns success with mapped courses`() = runTest {
        val dto = ResponseCourseDto(
            id = "1",
            title = "Test Course",
            description = "Desc",
            category = "Math",
            status = "PUBLISHED",
            visibility = "PUBLIC",
            createdBy = "user1"
        )
        val pageDto = PageResponseCourseDto(
            content = listOf(dto),
            totalPages = 1,
            totalElements = 1,
            number = 0,
            size = 10
        )
        whenever(enrollmentApi.getPublicCourses(0, 10)).thenReturn(Response.success(pageDto))

        val result = repository.getPublicCourses(0, 10)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.courses?.size)
        assertEquals("Test Course", result.getOrNull()?.courses?.first()?.title)
    }

    @Test
    fun `getPublicCourses returns empty list when body is null`() = runTest {
        whenever(enrollmentApi.getPublicCourses(0, 10)).thenReturn(Response.success(null))

        val result = repository.getPublicCourses(0, 10)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.courses?.size)
    }

    @Test
    fun `enrollInCourse returns success`() = runTest {
        whenever(enrollmentApi.enrollInCourse("course1")).thenReturn(
            Response.success(null)
        )

        val result = repository.enrollInCourse("course1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `enrollInCourse returns failure on exception`() = runTest {
        whenever(enrollmentApi.enrollInCourse("course1")).thenThrow(RuntimeException("Network error"))

        val result = repository.enrollInCourse("course1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `getPublicCourses returns failure on error response`() = runTest {
        whenever(enrollmentApi.getPublicCourses(0, 10)).thenReturn(
            Response.error(404, okhttp3.ResponseBody.create(null, ""))
        )

        val result = repository.getPublicCourses(0, 10)

        assertTrue(result.isFailure)
    }

    @Test
    fun `getPublicCourses returns failure on network exception`() = runTest {
        whenever(enrollmentApi.getPublicCourses(0, 10)).thenThrow(RuntimeException("Network error"))

        val result = repository.getPublicCourses(0, 10)

        assertTrue(result.isFailure)
    }

    @Test
    fun `enrollInCourse returns failure on error response`() = runTest {
        whenever(enrollmentApi.enrollInCourse("course1")).thenReturn(
            Response.error(409, okhttp3.ResponseBody.create(null, ""))
        )

        val result = repository.enrollInCourse("course1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `getPublicCourses maps all fields correctly`() = runTest {
        val dto = ResponseCourseDto(
            id = "1",
            title = "Math",
            description = null,
            category = null,
            status = "PUBLISHED",
            visibility = "PUBLIC",
            createdBy = null
        )
        val pageDto = PageResponseCourseDto(
            content = listOf(dto),
            totalPages = 1,
            totalElements = 1,
            number = 0,
            size = 10
        )
        whenever(enrollmentApi.getPublicCourses(0, 10)).thenReturn(Response.success(pageDto))

        val result = repository.getPublicCourses(0, 10)

        assertTrue(result.isSuccess)
        assertEquals("Math", result.getOrNull()?.courses?.first()?.title)
    }
}