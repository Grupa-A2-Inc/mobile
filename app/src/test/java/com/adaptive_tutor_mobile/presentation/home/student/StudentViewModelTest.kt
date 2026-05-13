package com.adaptive_tutor_mobile.presentation.home.student

import com.adaptive_tutor_mobile.data.remote.api.ProgressApi
import com.adaptive_tutor_mobile.data.remote.dto.EnrolledCourseDto
import com.adaptive_tutor_mobile.data.remote.dto.PageDto
import com.adaptive_tutor_mobile.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response

class StudentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val progressApi: ProgressApi = mock()

    @Test
    fun `loadEnrolledCourses success sets Success state`() = runTest {
        val page = PageDto(
            content = listOf(
                EnrolledCourseDto(
                    unrollmentId = "e1",
                    courseId = "c1",
                    courseTitle = "Course",
                    courseCategory = "Cat",
                    enrolledAt = "now",
                    progressPercent = 10.0,
                    completedAt = null
                )
            )
        )
        whenever(progressApi.getMyEnrolledCourses()).thenReturn(Response.success(page))

        val viewModel = StudentViewModel(progressApi)
        advanceUntilIdle()

        val state = viewModel.coursesState.value
        assertTrue(state is CoursesUiState.Success)
    }

    @Test
    fun `loadEnrolledCourses error sets Error state`() = runTest {
        val body = "oops".toResponseBody("text/plain".toMediaType())
        whenever(progressApi.getMyEnrolledCourses()).thenReturn(Response.error(500, body))

        val viewModel = StudentViewModel(progressApi)
        advanceUntilIdle()

        val state = viewModel.coursesState.value
        assertTrue(state is CoursesUiState.Error)
    }

    @Test
    fun `loadEnrolledCourses exception sets Error state`() = runTest {
        whenever(progressApi.getMyEnrolledCourses()).thenThrow(RuntimeException("boom"))

        val viewModel = StudentViewModel(progressApi)
        advanceUntilIdle()

        val state = viewModel.coursesState.value
        assertTrue(state is CoursesUiState.Error)
    }
}
