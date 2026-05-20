package com.adaptive_tutor_mobile

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.adaptive_tutor_mobile.domain.model.courses.Course
import com.adaptive_tutor_mobile.domain.model.courses.PagedCourses
import com.adaptive_tutor_mobile.domain.usecase.courses.EnrollInCourseUseCase
import com.adaptive_tutor_mobile.domain.usecase.courses.GetPublicCoursesUseCase
import com.adaptive_tutor_mobile.domain.usecase.courses.UnenrollFromCourseUseCase
import com.adaptive_tutor_mobile.presentation.courses.PublicCoursesViewModel
import com.adaptive_tutor_mobile.ProgressTestFixtures.enrolledDto
import com.adaptive_tutor_mobile.data.remote.api.ProgressApi
import com.adaptive_tutor_mobile.data.remote.dto.PageDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import retrofit2.Response
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    private val unenrollFromCourseUseCase: UnenrollFromCourseUseCase = mockk(relaxed = true)

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

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()

        assertEquals(1, viewModel.courses.value.size)
        assertEquals("Math", viewModel.courses.value.first().title)
    }

    @Test
    fun `loadCourses sets errorMessage on failure`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.failure(Exception("Network error")))

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()

        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `enroll sets enrollSuccess on success`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))
        whenever(enrollInCourseUseCase("course1")).thenReturn(Result.success(Unit))

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
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

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()

        viewModel.enroll("course1")
        advanceUntilIdle()

        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `clearEnrollSuccess sets enrollSuccess to null`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))
        whenever(enrollInCourseUseCase("course1")).thenReturn(Result.success(Unit))

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()

        viewModel.enroll("course1")
        advanceUntilIdle()

        viewModel.clearEnrollSuccess()

        assertNull(viewModel.enrollSuccess.value)
    }

    @Test
    fun `clearError sets errorMessage to null`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.failure(Exception("Error")))

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()

        viewModel.clearError()

        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `enroll adds courseId to enrolledCourseIds`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))
        whenever(enrollInCourseUseCase("course1")).thenReturn(Result.success(Unit))

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
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

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()

        viewModel.loadCourses(1, 10)
        advanceUntilIdle()

        assertTrue(viewModel.courses.value.isEmpty())
    }

    @Test
    fun `nextPage loads page 1 when totalPages is 2`() = runTest {
        val page0 = PagedCourses(listOf(Course("1", "A", "", "S", "PUBLISHED", "PUBLIC")), totalPages = 2, currentPage = 0)
        val page1 = PagedCourses(listOf(Course("2", "B", "", "S", "PUBLISHED", "PUBLIC")), totalPages = 2, currentPage = 1)
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(page0))
        whenever(getPublicCoursesUseCase(1, 10)).thenReturn(Result.success(page1))

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()

        viewModel.nextPage()
        advanceUntilIdle()

        assertEquals(1, viewModel.currentPage.value)
        assertEquals("B", viewModel.courses.value.first().title)
    }

    @Test
    fun `nextPage does nothing when already on last page`() = runTest {
        val pagedCourses = PagedCourses(emptyList(), totalPages = 1, currentPage = 0)
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(pagedCourses))

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()

        viewModel.nextPage()
        advanceUntilIdle()

        assertEquals(0, viewModel.currentPage.value)
    }

    @Test
    fun `previousPage decrements page when on page 1`() = runTest {
        val page0 = PagedCourses(emptyList(), totalPages = 2, currentPage = 0)
        val page1 = PagedCourses(emptyList(), totalPages = 2, currentPage = 1)
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(page0))
        whenever(getPublicCoursesUseCase(1, 10)).thenReturn(Result.success(page1))

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()
        viewModel.nextPage()
        advanceUntilIdle()

        viewModel.previousPage()
        advanceUntilIdle()

        assertEquals(0, viewModel.currentPage.value)
    }

    @Test
    fun `previousPage does nothing when already on page 0`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()

        viewModel.previousPage()
        advanceUntilIdle()

        assertEquals(0, viewModel.currentPage.value)
    }

    @Test
    fun `enroll already-enrolled response adds courseId and sets informational error`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))
        whenever(enrollInCourseUseCase("course1")).thenReturn(
            Result.failure(Exception("already enrolled in this course"))
        )

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()
        viewModel.enroll("course1")
        advanceUntilIdle()

        assertTrue(viewModel.enrolledCourseIds.value.contains("course1"))
        assertEquals("Ești deja înscris la acest curs", viewModel.errorMessage.value)
    }

    @Test
    fun `enroll 409 response adds courseId and sets informational error`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))
        whenever(enrollInCourseUseCase("course1")).thenReturn(
            Result.failure(Exception("409 Conflict"))
        )

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()
        viewModel.enroll("course1")
        advanceUntilIdle()

        assertTrue(viewModel.enrolledCourseIds.value.contains("course1"))
        assertEquals("Ești deja înscris la acest curs", viewModel.errorMessage.value)
    }

    @Test
    fun `enroll blank error message uses fallback`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))
        whenever(enrollInCourseUseCase("course1")).thenReturn(Result.failure(Exception("")))

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()
        viewModel.enroll("course1")
        advanceUntilIdle()

        assertEquals("Eroare la înscriere", viewModel.errorMessage.value)
    }

    // ── unenroll ──────────────────────────────────────────────────────────────

    @Test
    fun `unenroll success sets unenrollSuccess message`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))
        coEvery { unenrollFromCourseUseCase("c1") } returns Result.success(Unit)

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()
        viewModel.unenroll("c1")
        advanceUntilIdle()

        assertEquals("Dezabonat cu succes!", viewModel.unenrollSuccess.value)
    }

    @Test
    fun `unenroll success removes courseId from enrolledCourseIds`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))
        whenever(enrollInCourseUseCase("c1")).thenReturn(Result.success(Unit))
        coEvery { unenrollFromCourseUseCase("c1") } returns Result.success(Unit)

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()
        viewModel.enroll("c1")
        advanceUntilIdle()
        assertTrue(viewModel.enrolledCourseIds.value.contains("c1"))

        viewModel.unenroll("c1")
        advanceUntilIdle()

        assertFalse(viewModel.enrolledCourseIds.value.contains("c1"))
    }

    @Test
    fun `unenroll failure with message sets errorMessage`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))
        coEvery { unenrollFromCourseUseCase("c1") } returns Result.failure(Exception("Eroare dezabonare"))

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()
        viewModel.unenroll("c1")
        advanceUntilIdle()

        assertEquals("Eroare dezabonare", viewModel.errorMessage.value)
    }

    @Test
    fun `unenroll failure with blank message uses fallback`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))
        coEvery { unenrollFromCourseUseCase("c1") } returns Result.failure(Exception())

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()
        viewModel.unenroll("c1")
        advanceUntilIdle()

        assertEquals("Eroare la dezabonare", viewModel.errorMessage.value)
    }

    @Test
    fun `clearUnenrollSuccess resets to null`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))
        coEvery { unenrollFromCourseUseCase("c1") } returns Result.success(Unit)

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, mockk(relaxed = true))
        advanceUntilIdle()
        viewModel.unenroll("c1")
        advanceUntilIdle()
        assertNotNull(viewModel.unenrollSuccess.value)

        viewModel.clearUnenrollSuccess()

        assertNull(viewModel.unenrollSuccess.value)
    }

    // ── enrolledCourses ───────────────────────────────────────────────────────

    @Test
    fun `enrolledCourses and enrolledCourseIds populated from progressApi`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))
        val progressApi = mockk<ProgressApi>()
        coEvery { progressApi.getMyEnrolledCourses(any(), any(), any()) } returns Response.success(
            PageDto(content = listOf(enrolledDto(courseId = "c1", title = "Math")))
        )

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, progressApi)
        advanceUntilIdle()

        assertEquals(1, viewModel.enrolledCourses.value.size)
        assertEquals("c1", viewModel.enrolledCourses.value[0].courseId)
        assertTrue(viewModel.enrolledCourseIds.value.contains("c1"))
    }

    @Test
    fun `unenroll removes course from enrolledCourses list`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))
        val progressApi = mockk<ProgressApi>()
        coEvery { progressApi.getMyEnrolledCourses(any(), any(), any()) } returns Response.success(
            PageDto(content = listOf(enrolledDto(courseId = "c1", title = "Math")))
        )
        coEvery { unenrollFromCourseUseCase("c1") } returns Result.success(Unit)

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, progressApi)
        advanceUntilIdle()
        assertEquals(1, viewModel.enrolledCourses.value.size)

        viewModel.unenroll("c1")
        advanceUntilIdle()

        assertTrue(viewModel.enrolledCourses.value.isEmpty())
    }

    @Test
    fun `enrolledCourses empty when progressApi returns empty list`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))
        val progressApi = mockk<ProgressApi>()
        coEvery { progressApi.getMyEnrolledCourses(any(), any(), any()) } returns Response.success(
            PageDto(content = emptyList())
        )

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, progressApi)
        advanceUntilIdle()

        assertTrue(viewModel.enrolledCourses.value.isEmpty())
        assertTrue(viewModel.enrolledCourseIds.value.isEmpty())
    }

    @Test
    fun `enrolledCourses stays empty when progressApi fails`() = runTest {
        whenever(getPublicCoursesUseCase(0, 10)).thenReturn(Result.success(PagedCourses(emptyList(), 1, 0)))
        val progressApi = mockk<ProgressApi>()
        coEvery { progressApi.getMyEnrolledCourses(any(), any(), any()) } throws RuntimeException("offline")

        viewModel = PublicCoursesViewModel(getPublicCoursesUseCase, enrollInCourseUseCase, unenrollFromCourseUseCase, progressApi)
        advanceUntilIdle()

        assertTrue(viewModel.enrolledCourses.value.isEmpty())
    }
}