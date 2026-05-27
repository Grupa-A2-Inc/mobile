package com.adaptive_tutor_mobile.presentation.courses

import app.cash.turbine.test
import com.adaptive_tutor_mobile.domain.usecase.courses.DownloadCertificateUseCase
import com.adaptive_tutor_mobile.domain.usecase.courses.GetEnrolledCoursesUseCase
import com.adaptive_tutor_mobile.domain.usecase.courses.UnenrollFromCourseUseCase
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
    private lateinit var unenrollFromCourseUseCase: UnenrollFromCourseUseCase
    private lateinit var downloadCertificateUseCase: DownloadCertificateUseCase

    @Before
    fun setup() {
        useCase = mockk()
        unenrollFromCourseUseCase = mockk()
        downloadCertificateUseCase = mockk()
    }

    private fun viewModel() = EnrolledCoursesViewModel(useCase, unenrollFromCourseUseCase, downloadCertificateUseCase)

    @Test
    fun `initial state is Loading before init runs`() = runTest {
        coEvery { useCase() } returns Result.success(emptyList())
        coEvery { unenrollFromCourseUseCase(any()) } returns Result.success(Unit)

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
        coEvery { unenrollFromCourseUseCase(any()) } returns Result.success(Unit)

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
        coEvery { unenrollFromCourseUseCase(any()) } returns Result.success(Unit)

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is EnrolledCoursesUiState.Success)
        assertTrue((state as EnrolledCoursesUiState.Success).courses.isEmpty())
    }

    @Test
    fun `init failure populates Error state with message`() = runTest {
        coEvery { useCase() } returns Result.failure(IllegalStateException("Sesiune expirată"))
        coEvery { unenrollFromCourseUseCase(any()) } returns Result.success(Unit)

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is EnrolledCoursesUiState.Error)
        assertEquals("Sesiune expirată", (state as EnrolledCoursesUiState.Error).message)
    }

    @Test
    fun `init failure with null message uses fallback`() = runTest {
        coEvery { useCase() } returns Result.failure(RuntimeException())
        coEvery { unenrollFromCourseUseCase(any()) } returns Result.success(Unit)

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is EnrolledCoursesUiState.Error)
        assertEquals("Nu am putut încărca cursurile", (state as EnrolledCoursesUiState.Error).message)
    }

    @Test
    fun `loadCourses transitions through Loading then Success`() = runTest {
        coEvery { useCase() } returns Result.success(listOf(domainCourse()))
        coEvery { unenrollFromCourseUseCase(any()) } returns Result.success(Unit)

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
        coEvery { unenrollFromCourseUseCase(any()) } returns Result.success(Unit)

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

    @Test
    fun `loadCourses can transition from success back to error`() = runTest {
        coEvery { useCase() } returnsMany listOf(
            Result.success(listOf(domainCourse(courseId = "c1"))),
            Result.failure(IllegalStateException("retry failed"))
        )
        coEvery { unenrollFromCourseUseCase(any()) } returns Result.success(Unit)

        val vm = viewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is EnrolledCoursesUiState.Success)

        vm.loadCourses()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is EnrolledCoursesUiState.Error)
        assertEquals("retry failed", (state as EnrolledCoursesUiState.Error).message)
    }

    @Test
    fun `unenroll removes course from success state and sets snackbar message`() = runTest {
        val courses = listOf(
            domainCourse(courseId = "c1", title = "Math"),
            domainCourse(courseId = "c2", title = "Physics")
        )
        coEvery { useCase() } returns Result.success(courses)
        coEvery { unenrollFromCourseUseCase("c1") } returns Result.success(Unit)

        val vm = viewModel()
        advanceUntilIdle()

        vm.unenroll("c1")
        advanceUntilIdle()

        val state = vm.uiState.value as EnrolledCoursesUiState.Success
        assertEquals(1, state.courses.size)
        assertEquals("c2", state.courses.first().courseId)
        assertEquals("Te-ai dezabonat", vm.unenrollSuccess.value)
    }

    @Test
    fun `unenroll keeps current list and sets error message on failure`() = runTest {
        val courses = listOf(
            domainCourse(courseId = "c1", title = "Math"),
            domainCourse(courseId = "c2", title = "Physics")
        )
        coEvery { useCase() } returns Result.success(courses)
        coEvery { unenrollFromCourseUseCase("c1") } returns Result.failure(Exception("Error"))

        val vm = viewModel()
        advanceUntilIdle()

        vm.unenroll("c1")
        advanceUntilIdle()

        val state = vm.uiState.value as EnrolledCoursesUiState.Success
        assertEquals(2, state.courses.size)
        assertEquals("Error", vm.errorMessage.value)
    }

    @Test
    fun `unenroll can fail after a previous success`() = runTest {
        val courses = listOf(
            domainCourse(courseId = "c1", title = "Math"),
            domainCourse(courseId = "c2", title = "Physics")
        )
        coEvery { useCase() } returns Result.success(courses)
        coEvery { unenrollFromCourseUseCase("c1") } returns Result.success(Unit)
        coEvery { unenrollFromCourseUseCase("c2") } returns Result.failure(Exception("second failed"))

        val vm = viewModel()
        advanceUntilIdle()

        vm.unenroll("c1")
        advanceUntilIdle()
        vm.unenroll("c2")
        advanceUntilIdle()

        assertEquals("second failed", vm.errorMessage.value)
    }

    @Test
    fun `unenroll returns early when state is not success`() = runTest {
        coEvery { useCase() } returns Result.failure(Exception("boom"))
        coEvery { unenrollFromCourseUseCase(any()) } returns Result.success(Unit)

        val vm = viewModel()
        advanceUntilIdle()

        vm.unenroll("c1")
        advanceUntilIdle()

        coVerify(exactly = 0) { unenrollFromCourseUseCase(any()) }
    }

    @Test
    fun `unenroll failure with null message uses fallback`() = runTest {
        val courses = listOf(domainCourse(courseId = "c1", title = "Math"))
        coEvery { useCase() } returns Result.success(courses)
        coEvery { unenrollFromCourseUseCase("c1") } returns Result.failure(Exception())

        val vm = viewModel()
        advanceUntilIdle()

        vm.unenroll("c1")
        advanceUntilIdle()

        assertEquals("Nu am putut procesa dezabonarea", vm.errorMessage.value)
    }

    @Test
    fun `clear helpers reset transient messages`() = runTest {
        val courses = listOf(domainCourse(courseId = "c1", title = "Math"))
        coEvery { useCase() } returns Result.success(courses)
        coEvery { unenrollFromCourseUseCase("c1") } returns Result.success(Unit)

        val vm = viewModel()
        advanceUntilIdle()

        vm.unenroll("c1")
        advanceUntilIdle()
        vm.clearErrorMessage()
        vm.clearUnenrollSuccess()

        assertEquals(null, vm.errorMessage.value)
        assertEquals(null, vm.unenrollSuccess.value)
    }
}
