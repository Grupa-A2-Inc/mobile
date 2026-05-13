package com.adaptive_tutor_mobile.presentation.courses

import androidx.lifecycle.SavedStateHandle
import com.adaptive_tutor_mobile.domain.model.Chapter
import com.adaptive_tutor_mobile.domain.model.CourseDetail
import com.adaptive_tutor_mobile.domain.model.LessonSummary
import com.adaptive_tutor_mobile.domain.usecase.GetCourseFullViewUseCase
import com.adaptive_tutor_mobile.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CourseDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCase: GetCourseFullViewUseCase = mock()

    @Test
    fun `loadCourseDetail success updates state`() = runTest {
        val detail = CourseDetail(
            id = "c1",
            title = "Course",
            description = "Desc",
            chapters = listOf(
                Chapter(
                    id = "ch1",
                    title = "Chapter",
                    lessons = listOf(LessonSummary(id = "l1", title = "Lesson", hasTest = false))
                )
            )
        )
        whenever(useCase("c1")).thenReturn(Result.success(detail))

        val viewModel = CourseDetailViewModel(SavedStateHandle(mapOf("courseId" to "c1")), useCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(detail, state.courseDetail)
        assertTrue(!state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadCourseDetail failure sets error`() = runTest {
        whenever(useCase("c1")).thenReturn(Result.failure(RuntimeException("boom")))

        val viewModel = CourseDetailViewModel(SavedStateHandle(mapOf("courseId" to "c1")), useCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.error != null)
        assertTrue(!state.isLoading)
    }

    @Test
    fun `toggleChapter adds and removes ids`() {
        val viewModel = CourseDetailViewModel(SavedStateHandle(mapOf("courseId" to "c1")), useCase)

        viewModel.toggleChapter("ch1")
        assertTrue(viewModel.uiState.value.expandedChapters.contains("ch1"))

        viewModel.toggleChapter("ch1")
        assertTrue(!viewModel.uiState.value.expandedChapters.contains("ch1"))
    }
}
