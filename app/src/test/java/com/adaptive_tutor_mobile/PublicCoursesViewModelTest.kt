package com.adaptive_tutor_mobile

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.adaptive_tutor_mobile.domain.model.Course
import com.adaptive_tutor_mobile.domain.model.PagedCourses
import com.adaptive_tutor_mobile.domain.usecase.EnrollInCourseUseCase
import com.adaptive_tutor_mobile.domain.usecase.GetPublicCoursesUseCase
import com.adaptive_tutor_mobile.presentation.courses.PublicCoursesViewModel
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.Assert.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class PublicCoursesViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private val getPublicCoursesUseCase: GetPublicCoursesUseCase = mock()
    private val enrollInCourseUseCase: EnrollInCourseUseCase = mock()

    private lateinit var viewModel: PublicCoursesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadCourses sets courses on success`() = runTest {
        val pagedCourses = PagedCourses(
            courses = listOf(Course("1", "Math", "Desc", "Science", "PUBLISHED", "PUBLIC")),
            totalPages = 1,
            currentPage = 0
        )
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(pagedCourses))

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()

        assertEquals(1, viewModel.courses.value.size)
        assertEquals("Math", viewModel.courses.value.first().title)
    }

    @Test
    fun `loadCourses sets errorMessage on failure`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.failure(Exception("Network error")))

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()

        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `enroll sets enrollSuccess on success`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))
        whenever(enrollInCourseUseCase("course1")).thenReturn(Result.success(Unit))

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()

        viewModel.enroll("course1")
        advanceUntilIdle()

        assertNotNull(viewModel.enrollSuccess.value)
        assertTrue(viewModel.enrolledCourseIds.value.contains("course1"))
    }

    @Test
    fun `enroll sets errorMessage on failure`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))
        whenever(enrollInCourseUseCase("course1")).thenReturn(Result.failure(Exception("Error")))

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()

        viewModel.enroll("course1")
        advanceUntilIdle()

        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `clearEnrollSuccess sets enrollSuccess to null`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))
        whenever(enrollInCourseUseCase("course1")).thenReturn(Result.success(Unit))

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()

        viewModel.enroll("course1")
        advanceUntilIdle()

        viewModel.clearEnrollSuccess()

        assertNull(viewModel.enrollSuccess.value)
    }

    @Test
    fun `clearError sets errorMessage to null`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.failure(Exception("Error")))

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()

        viewModel.clearError()

        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `enroll adds courseId to enrolledCourseIds`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))
        whenever(enrollInCourseUseCase("course1")).thenReturn(Result.success(Unit))

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()

        viewModel.enroll("course1")
        advanceUntilIdle()

        assertTrue(viewModel.enrolledCourseIds.value.contains("course1"))
    }

    @Test
    fun `loadCourses with different page and size`() = runTest {
        val pagedCourses = PagedCourses(emptyList(), 1, 0)
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(pagedCourses))
        whenever(getPublicCoursesUseCase(1, 10)).thenReturn(Result.success(pagedCourses))

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()

        viewModel.loadCourses(1, 10)
        advanceUntilIdle()

        assertTrue(viewModel.courses.value.isEmpty())
    }
}