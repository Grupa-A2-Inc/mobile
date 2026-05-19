package com.adaptive_tutor_mobile.presentation.home.student

import com.adaptive_tutor_mobile.ProgressTestFixtures.enrolledDto
import com.adaptive_tutor_mobile.data.remote.api.ProgressApi
import com.adaptive_tutor_mobile.data.remote.dto.PageDto
import com.adaptive_tutor_mobile.domain.repository.CourseRepository
import com.adaptive_tutor_mobile.domain.usecase.UnenrollFromCourseUseCase
import com.adaptive_tutor_mobile.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class StudentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val progressApi: ProgressApi = mock()
    private val unenrollFromCourseUseCase: UnenrollFromCourseUseCase = mock()

    @Test
    fun `loadEnrolledCourses success sets Success state`() = runTest {
        val page = PageDto(
            content = listOf(
                enrolledDto(
                    unrollmentId = "e1",
                    courseId = "c1",
                    title = "Course",
                    category = "Cat",
                    enrolledAt = "now",
                    progress = 10.0
                )
            )
        )
        whenever(progressApi.getMyEnrolledCourses()).thenReturn(Response.success(page))

        val viewModel = StudentViewModel(progressApi, unenrollFromCourseUseCase)
        advanceUntilIdle()

        val state = viewModel.coursesState.value
        assertTrue(state is CoursesUiState.Success)
    }

    @Test
    fun `loadEnrolledCourses error sets Error state`() = runTest {
        val body = "oops".toResponseBody("text/plain".toMediaType())
        whenever(progressApi.getMyEnrolledCourses()).thenReturn(Response.error(500, body))

        val viewModel = StudentViewModel(progressApi, unenrollFromCourseUseCase)
        advanceUntilIdle()

        val state = viewModel.coursesState.value
        assertTrue(state is CoursesUiState.Error)
    }

    @Test
    fun `loadEnrolledCourses exception sets Error state`() = runTest {
        whenever(progressApi.getMyEnrolledCourses()).thenThrow(RuntimeException("boom"))

        val viewModel = StudentViewModel(progressApi, unenrollFromCourseUseCase)
        advanceUntilIdle()

        val state = viewModel.coursesState.value
        assertTrue(state is CoursesUiState.Error)
    }

    @Test
    fun `loadEnrolledCourses exception without message uses unknown error fallback`() = runTest {
        whenever(progressApi.getMyEnrolledCourses()).thenThrow(RuntimeException())

        val viewModel = StudentViewModel(progressApi, unenrollFromCourseUseCase)
        advanceUntilIdle()

        val state = viewModel.coursesState.value as CoursesUiState.Error
        assertEquals("Eroare necunoscută", state.message)
    }

    @Test
    fun `loadEnrolledCourses with null body content sets empty success list`() = runTest {
        whenever(progressApi.getMyEnrolledCourses()).thenReturn(Response.success(PageDto(content = emptyList())))

        val viewModel = StudentViewModel(progressApi, unenrollFromCourseUseCase)
        advanceUntilIdle()

        val state = viewModel.coursesState.value
        assertTrue(state is CoursesUiState.Success)
        assertTrue((state as CoursesUiState.Success).courses.isEmpty())
    }

    @Test
    fun `loadEnrolledCourses with null body sets empty success list`() = runTest {
        @Suppress("UNCHECKED_CAST")
        whenever(progressApi.getMyEnrolledCourses())
            .thenReturn(Response.success(null) as Response<PageDto<com.adaptive_tutor_mobile.data.remote.dto.EnrolledCourseDto>>)

        val viewModel = StudentViewModel(progressApi, unenrollFromCourseUseCase)
        advanceUntilIdle()

        val state = viewModel.coursesState.value
        assertTrue(state is CoursesUiState.Success)
        assertTrue((state as CoursesUiState.Success).courses.isEmpty())
    }

    @Test
    fun `loadEnrolledCourses with null content in body sets empty success list`() = runTest {
        val page = mock<PageDto<com.adaptive_tutor_mobile.data.remote.dto.EnrolledCourseDto>>()
        @Suppress("UNCHECKED_CAST")
        whenever(page.content).thenReturn(null as List<com.adaptive_tutor_mobile.data.remote.dto.EnrolledCourseDto>?)
        whenever(progressApi.getMyEnrolledCourses()).thenReturn(Response.success(page))

        val viewModel = StudentViewModel(progressApi, unenrollFromCourseUseCase)
        advanceUntilIdle()

        val state = viewModel.coursesState.value
        assertTrue(state is CoursesUiState.Success)
        assertTrue((state as CoursesUiState.Success).courses.isEmpty())
    }

    @Test
    fun `unenroll success removes course and sets confirmation message`() = runTest {
        val page = PageDto(
            content = listOf(
                enrolledDto(courseId = "c1", title = "Course 1"),
                enrolledDto(courseId = "c2", title = "Course 2", unrollmentId = "e2")
            )
        )
        whenever(progressApi.getMyEnrolledCourses()).thenReturn(Response.success(page))
        whenever(unenrollFromCourseUseCase.invoke("c1")).thenReturn(Result.success(Unit))

        val viewModel = StudentViewModel(progressApi, unenrollFromCourseUseCase)
        advanceUntilIdle()

        viewModel.unenroll("c1")
        advanceUntilIdle()

        val state = viewModel.coursesState.value as CoursesUiState.Success
        assertEquals(listOf("c2"), state.courses.map { it.courseId })
        assertEquals("Te-ai dezabonat", viewModel.message.value)
        verify(unenrollFromCourseUseCase).invoke("c1")
    }

    @Test
    fun `unenroll success after suspension removes course and sets confirmation message`() = runTest {
        val page = PageDto(content = listOf(enrolledDto(courseId = "c1"), enrolledDto(courseId = "c2")))
        val courseRepository = mockk<CourseRepository>()
        val suspendingUseCase = UnenrollFromCourseUseCase(courseRepository)
        whenever(progressApi.getMyEnrolledCourses()).thenReturn(Response.success(page))
        coEvery { courseRepository.unenrollFromCourse("c1") } coAnswers {
            delay(1)
            Result.success(Unit)
        }

        val viewModel = StudentViewModel(progressApi, suspendingUseCase)
        advanceUntilIdle()

        viewModel.unenroll("c1")
        advanceUntilIdle()

        val state = viewModel.coursesState.value as CoursesUiState.Success
        assertEquals(listOf("c2"), state.courses.map { it.courseId })
        assertEquals("Te-ai dezabonat", viewModel.message.value)
    }

    @Test
    fun `unenroll failure keeps courses and exposes error message`() = runTest {
        val page = PageDto(content = listOf(enrolledDto(courseId = "c1")))
        whenever(progressApi.getMyEnrolledCourses()).thenReturn(Response.success(page))
        whenever(unenrollFromCourseUseCase.invoke("c1"))
            .thenReturn(Result.failure(IllegalStateException("Nu merge")))

        val viewModel = StudentViewModel(progressApi, unenrollFromCourseUseCase)
        advanceUntilIdle()

        viewModel.unenroll("c1")
        advanceUntilIdle()

        val state = viewModel.coursesState.value as CoursesUiState.Success
        assertEquals(listOf("c1"), state.courses.map { it.courseId })
        assertEquals("Nu merge", viewModel.message.value)
    }

    @Test
    fun `unenroll failure without message uses fallback message`() = runTest {
        val page = PageDto(content = listOf(enrolledDto(courseId = "c1")))
        whenever(progressApi.getMyEnrolledCourses()).thenReturn(Response.success(page))
        whenever(unenrollFromCourseUseCase.invoke("c1"))
            .thenReturn(Result.failure(IllegalStateException()))

        val viewModel = StudentViewModel(progressApi, unenrollFromCourseUseCase)
        advanceUntilIdle()

        viewModel.unenroll("c1")
        advanceUntilIdle()

        assertEquals("Nu am putut procesa dezabonarea", viewModel.message.value)
    }

    @Test
    fun `unenroll exception from use case uses fallback message`() = runTest {
        val page = PageDto(content = listOf(enrolledDto(courseId = "c1")))
        whenever(progressApi.getMyEnrolledCourses()).thenReturn(Response.success(page))
        whenever(unenrollFromCourseUseCase.invoke(any())).thenThrow(IllegalStateException("crash"))

        val viewModel = StudentViewModel(progressApi, unenrollFromCourseUseCase)
        advanceUntilIdle()

        viewModel.unenroll("c1")
        advanceUntilIdle()

        assertEquals("crash", viewModel.message.value)
    }

    @Test
    fun `unenroll exception without message after suspension uses fallback message`() = runTest {
        val page = PageDto(content = listOf(enrolledDto(courseId = "c1")))
        val courseRepository = mockk<CourseRepository>()
        val suspendingUseCase = UnenrollFromCourseUseCase(courseRepository)
        whenever(progressApi.getMyEnrolledCourses()).thenReturn(Response.success(page))
        coEvery { courseRepository.unenrollFromCourse("c1") } coAnswers {
            delay(1)
            throw IllegalStateException()
        }

        val viewModel = StudentViewModel(progressApi, suspendingUseCase)
        advanceUntilIdle()

        viewModel.unenroll("c1")
        advanceUntilIdle()

        assertEquals("Nu am putut procesa dezabonarea", viewModel.message.value)
    }

    @Test
    fun `unenroll ignored when current state is not success`() = runTest {
        val body = "oops".toResponseBody("text/plain".toMediaType())
        whenever(progressApi.getMyEnrolledCourses()).thenReturn(Response.error(500, body))

        val viewModel = StudentViewModel(progressApi, unenrollFromCourseUseCase)
        advanceUntilIdle()

        viewModel.unenroll("c1")
        advanceUntilIdle()

        verify(progressApi).getMyEnrolledCourses()
        assertNull(viewModel.message.value)
        assertTrue(viewModel.coursesState.value is CoursesUiState.Error)
    }

    @Test
    fun `clearMessage resets message to null`() = runTest {
        val page = PageDto(content = listOf(enrolledDto(courseId = "c1")))
        whenever(progressApi.getMyEnrolledCourses()).thenReturn(Response.success(page))
        whenever(unenrollFromCourseUseCase.invoke("c1")).thenReturn(Result.success(Unit))

        val viewModel = StudentViewModel(progressApi, unenrollFromCourseUseCase)
        advanceUntilIdle()
        viewModel.unenroll("c1")
        advanceUntilIdle()

        viewModel.clearMessage()

        assertNull(viewModel.message.value)
    }
}
