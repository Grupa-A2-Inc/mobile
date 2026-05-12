package com.adaptive_tutor_mobile.presentation.courses

import app.cash.turbine.test
import com.adaptive_tutor_mobile.domain.usecase.GetEnrolledCoursesUseCase
import com.adaptive_tutor_mobile.testing.MainDispatcherRule
import com.adaptive_tutor_mobile.ProgressTestFixtures.domainCourse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EnrolledCoursesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var useCase: GetEnrolledCoursesUseCase

    @Before
    fun setup() {
        useCase = mockk()
    }

    private fun viewModel() = EnrolledCoursesViewModel(useCase)

    @Test
    fun `initial state is Loading before init runs`() = runTest {
        coEvery { useCase() } returns Result.success(emptyList())

        val vm = viewModel()
        // before advanceUntilIdle, init coroutine hasn't run yet
        assertTrue(vm.uiState.value is EnrolledCoursesUiState.Loading)
    }

    @Test
    fun `init success populates Success state with courses`() = runTest {
        val courses = listOf(
            domainCourse(courseId = "c1", title = "Math"),
            domainCourse(courseId = "c2", title = "Physics", progress = 80.0)
        )
        coEvery { useCase() } returns Result.success(courses)

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is EnrolledCoursesUiState.Success)
        assertEquals(2, (state as EnrolledCoursesUiState.Success).courses.size)
        assertEquals("c1", state.courses[0].courseId)
    }

    @Test
    fun `init success with empty list yields empty Success`() = runTest {
        coEvery { useCase() } returns Result.success(emptyList())

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is EnrolledCoursesUiState.Success)
        assertTrue((state as EnrolledCoursesUiState.Success).courses.isEmpty())
    }

    @Test
    fun `init failure populates Error state with message`() = runTest {
        coEvery { useCase() } returns Result.failure(IllegalStateException("Sesiune expirată"))

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is EnrolledCoursesUiState.Error)
        assertEquals("Sesiune expirată", (state as EnrolledCoursesUiState.Error).message)
    }

    @Test
    fun `init failure with null message uses fallback`() = runTest {
        coEvery { useCase() } returns Result.failure(RuntimeException())

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is EnrolledCoursesUiState.Error)
        assertEquals("Nu am putut încărca cursurile", (state as EnrolledCoursesUiState.Error).message)
    }

    @Test
    fun `loadCourses transitions through Loading then Success`() = runTest {
        coEvery { useCase() } returns Result.success(listOf(domainCourse()))

        val vm = viewModel()
        advanceUntilIdle()

        vm.uiState.test {
            // Already in Success after init
            assertTrue(awaitItem() is EnrolledCoursesUiState.Success)

            vm.loadCourses()

            assertTrue(awaitItem() is EnrolledCoursesUiState.Loading)
            assertTrue(awaitItem() is EnrolledCoursesUiState.Success)
        }
    }

    @Test
    fun `loadCourses retries after error and recovers to Success`() = runTest {
        coEvery { useCase() } returnsMany listOf(
            Result.failure(IllegalStateException("Network")),
            Result.success(listOf(domainCourse(courseId = "c1")))
        )

        val vm = viewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is EnrolledCoursesUiState.Error)

        vm.loadCourses()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is EnrolledCoursesUiState.Success)
        assertEquals("c1", (state as EnrolledCoursesUiState.Success).courses[0].courseId)
        coVerify(exactly = 2) { useCase() }
    }
}