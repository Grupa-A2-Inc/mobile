package com.adaptive_tutor_mobile.presentation.lesson

import androidx.lifecycle.SavedStateHandle
import com.adaptive_tutor_mobile.domain.model.LessonDetail
import com.adaptive_tutor_mobile.domain.model.LessonResource
import com.adaptive_tutor_mobile.domain.repository.LessonRepository
import com.adaptive_tutor_mobile.domain.usecase.GetLessonDetailUseCase
import com.adaptive_tutor_mobile.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class LessonViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getLessonDetailUseCase: GetLessonDetailUseCase = mock()
    private val lessonRepository: LessonRepository = mock()
    private val submitLessonRatingUseCase: com.adaptive_tutor_mobile.domain.usecase.SubmitLessonRatingUseCase = mock()
    private val ratingRepository: com.adaptive_tutor_mobile.domain.repository.RatingRepository = mock()

    @Test
    fun `loadLesson success updates state and checks test`() = runTest {
        val detail = LessonDetail(
            id = "l1",
            title = "Lesson",
            contentMarkdown = "# Markdown",
            resources = listOf(LessonResource("r1", "Doc", "https://x"))
        )
        whenever(getLessonDetailUseCase("l1")).thenReturn(Result.success(detail))
        whenever(lessonRepository.checkLessonTest("l1")).thenReturn("t1")
        whenever(lessonRepository.markVisited("l1")).thenReturn(Unit)

        val viewModel = LessonViewModel(
            getLessonDetailUseCase = getLessonDetailUseCase,
            lessonRepository = lessonRepository,
            submitLessonRatingUseCase = submitLessonRatingUseCase,
            ratingRepository = ratingRepository,
            savedStateHandle = SavedStateHandle(mapOf("lessonId" to "l1"))
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("l1", state.lesson?.id)
        assertEquals("t1", state.testId)
        assertTrue(!state.isLoading)
        assertTrue(!state.isCheckingTest)
        verify(lessonRepository).markVisited("l1")
    }

    @Test
    fun `loadLesson failure sets error`() = runTest {
        whenever(getLessonDetailUseCase("l1")).thenReturn(Result.failure(RuntimeException("boom")))

        val viewModel = LessonViewModel(
            getLessonDetailUseCase = getLessonDetailUseCase,
            lessonRepository = lessonRepository,
            submitLessonRatingUseCase = submitLessonRatingUseCase,
            ratingRepository = ratingRepository,
            savedStateHandle = SavedStateHandle(mapOf("lessonId" to "l1"))
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.error)
        assertNull(state.lesson)
    }

    @Test
    fun `loadLesson failure with null message uses fallback`() = runTest {
        whenever(getLessonDetailUseCase("l1")).thenReturn(Result.failure(RuntimeException()))

        val viewModel = LessonViewModel(
            getLessonDetailUseCase = getLessonDetailUseCase,
            lessonRepository = lessonRepository,
            submitLessonRatingUseCase = submitLessonRatingUseCase,
            ratingRepository = ratingRepository,
            savedStateHandle = SavedStateHandle(mapOf("lessonId" to "l1"))
        )
        advanceUntilIdle()

        assertEquals("Eroare necunoscută", viewModel.state.value.error)
    }

    @Test
    fun `no lessonId in saved state leaves initial state unchanged`() = runTest {
        val viewModel = LessonViewModel(
            getLessonDetailUseCase = getLessonDetailUseCase,
            lessonRepository = lessonRepository,
            submitLessonRatingUseCase = submitLessonRatingUseCase,
            ratingRepository = ratingRepository,
            savedStateHandle = SavedStateHandle(emptyMap())
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.lesson)
        assertNull(state.error)
        assertTrue(!state.isLoading)
    }

    @Test
    fun `checkLessonTest returns null when no test available`() = runTest {
        val detail = LessonDetail("l1", "Lesson", "# Md", emptyList())
        whenever(getLessonDetailUseCase("l1")).thenReturn(Result.success(detail))
        whenever(lessonRepository.checkLessonTest("l1")).thenReturn(null)
        whenever(lessonRepository.markVisited("l1")).thenReturn(Unit)

        val viewModel = LessonViewModel(
            getLessonDetailUseCase = getLessonDetailUseCase,
            lessonRepository = lessonRepository,
            submitLessonRatingUseCase = submitLessonRatingUseCase,
            ratingRepository = ratingRepository,
            savedStateHandle = SavedStateHandle(mapOf("lessonId" to "l1"))
        )
        advanceUntilIdle()

        assertNull(viewModel.state.value.testId)
        assertTrue(!viewModel.state.value.isCheckingTest)
    }

    @Test
    fun `showRatingDialog sets showRatingDialog to true`() = runTest {
        val viewModel = LessonViewModel(
            getLessonDetailUseCase = getLessonDetailUseCase,
            lessonRepository = lessonRepository,
            submitLessonRatingUseCase = submitLessonRatingUseCase,
            ratingRepository = ratingRepository,
            savedStateHandle = SavedStateHandle(emptyMap())
        )

        viewModel.showRatingDialog()

        assertTrue(viewModel.state.value.showRatingDialog)
    }

    @Test
    fun `dismissRatingDialog sets showRatingDialog to false`() = runTest {
        val viewModel = LessonViewModel(
            getLessonDetailUseCase = getLessonDetailUseCase,
            lessonRepository = lessonRepository,
            submitLessonRatingUseCase = submitLessonRatingUseCase,
            ratingRepository = ratingRepository,
            savedStateHandle = SavedStateHandle(emptyMap())
        )

        viewModel.showRatingDialog()
        viewModel.dismissRatingDialog()

        assertTrue(!viewModel.state.value.showRatingDialog)
    }

    @Test
    fun `submitRating sets hasRated to true on success`() = runTest {
        whenever(submitLessonRatingUseCase("l1", 5, null)).thenReturn(Result.success(Unit))
        whenever(ratingRepository.getRatingSummary("l1")).thenReturn(
            Result.success(com.adaptive_tutor_mobile.domain.model.RatingSummary(5.0f, 1))
        )

        val viewModel = LessonViewModel(
            getLessonDetailUseCase = getLessonDetailUseCase,
            lessonRepository = lessonRepository,
            submitLessonRatingUseCase = submitLessonRatingUseCase,
            ratingRepository = ratingRepository,
            savedStateHandle = SavedStateHandle(emptyMap())
        )

        viewModel.submitRating("l1", 5, null)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.hasRated)
        assertTrue(!viewModel.state.value.showRatingDialog)
    }
}
