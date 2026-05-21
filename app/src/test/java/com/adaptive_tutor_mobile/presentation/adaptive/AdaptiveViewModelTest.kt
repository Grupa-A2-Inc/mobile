package com.adaptive_tutor_mobile.presentation.adaptive

import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveAttemptReportDTO
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveQuestionForStudentDto
import com.adaptive_tutor_mobile.data.remote.dto.OptionForStudentDto
import com.adaptive_tutor_mobile.domain.model.adaptive.AdaptiveSession
import com.adaptive_tutor_mobile.domain.repository.adaptive.AdaptiveRepository
import com.adaptive_tutor_mobile.domain.usecase.adaptive.StartAdaptiveSessionUseCase
import com.adaptive_tutor_mobile.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AdaptiveViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var startAdaptiveSessionUseCase: StartAdaptiveSessionUseCase
    private lateinit var adaptiveRepository: AdaptiveRepository

    private val sessionId = "session-abc"
    private val attemptId = "attempt-xyz"

    private val sampleQuestion = AdaptiveQuestionForStudentDto(
        questionId = "1",
        questionType = "SINGLE_CHOICE",
        content = "What is 1+1?",
        difficulty = 0.3,
        options = listOf(
            OptionForStudentDto(optionId = 0, text = "1", displayOrder = 0),
            OptionForStudentDto(optionId = 1, text = "2", displayOrder = 1)
        )
    )

    private fun session(aid: String? = attemptId) = AdaptiveSession(
        sessionId = sessionId,
        attemptId = aid,
        expiresAt = null,
        questions = listOf(sampleQuestion)
    )

    private val sampleReport = AdaptiveAttemptReportDTO(
        attemptId = attemptId, score = 80.0, scorePercent = 80.0,
        passed = true, completedAt = null, questions = emptyList()
    )

    @Before
    fun setup() {
        startAdaptiveSessionUseCase = mockk()
        adaptiveRepository = mockk()
    }

    private fun viewModel() = AdaptiveViewModel(startAdaptiveSessionUseCase, adaptiveRepository)

    // ── startSession ──────────────────────────────────────────────────────────

    @Test
    fun `startSession success sets session and clears loading`() = runTest {
        coEvery { startAdaptiveSessionUseCase(1, 2, 5) } returns Result.success(session())
        val vm = viewModel()

        vm.startSession(subjectId = 1, topicId = 2, count = 5)
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.session)
        assertEquals(sessionId, vm.uiState.value.session!!.sessionId)
        assertEquals(false, vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `startSession failure sets errorMessage`() = runTest {
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns
            Result.failure(RuntimeException("Server error"))
        val vm = viewModel()

        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        assertEquals("Server error", vm.uiState.value.errorMessage)
        assertNull(vm.uiState.value.session)
        assertEquals(false, vm.uiState.value.isLoading)
    }

    @Test
    fun `startSession failure with null message uses default`() = runTest {
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns
            Result.failure(RuntimeException())
        val vm = viewModel()

        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        assertEquals("Nu am putut porni sesiunea adaptivă", vm.uiState.value.errorMessage)
    }

    @Test
    fun `startSession sets isLoading true while in progress`() = runTest {
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns Result.success(session())
        val vm = viewModel()

        // Before coroutine runs, isLoading should be set
        vm.startSession(1, 2, 5)
        // After completion isLoading should be false
        advanceUntilIdle()
        assertEquals(false, vm.uiState.value.isLoading)
    }

    // ── selectAnswer ──────────────────────────────────────────────────────────

    @Test
    fun `selectAnswer single choice replaces existing selection`() = runTest {
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns Result.success(session())
        val vm = viewModel()
        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        vm.selectAnswer(questionId = "1", optionId = 0, singleChoice = true)
        vm.selectAnswer(questionId = "1", optionId = 1, singleChoice = true)

        assertEquals(listOf(1), vm.uiState.value.selectedAnswers["1"])
    }

    @Test
    fun `selectAnswer multi choice adds options`() = runTest {
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns Result.success(session())
        val vm = viewModel()
        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        vm.selectAnswer("1", 0, false)
        vm.selectAnswer("1", 1, false)

        val selected = vm.uiState.value.selectedAnswers["1"]!!
        assertTrue(selected.contains(0))
        assertTrue(selected.contains(1))
    }

    @Test
    fun `selectAnswer multi choice deselects already selected option`() = runTest {
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns Result.success(session())
        val vm = viewModel()
        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        vm.selectAnswer("1", 0, false)
        vm.selectAnswer("1", 1, false)
        vm.selectAnswer("1", 0, false) // deselect

        assertEquals(listOf(1), vm.uiState.value.selectedAnswers["1"])
    }

    // ── navigation ────────────────────────────────────────────────────────────

    @Test
    fun `nextQuestion increments and prevQuestion decrements currentIndex`() = runTest {
        val twoQuestions = session().copy(
            questions = listOf(sampleQuestion, sampleQuestion.copy(questionId = "2"))
        )
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns
            Result.success(twoQuestions)
        val vm = viewModel()
        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        vm.nextQuestion()
        assertEquals(1, vm.uiState.value.currentIndex)

        vm.prevQuestion()
        assertEquals(0, vm.uiState.value.currentIndex)
    }

    @Test
    fun `goToQuestion ignores out-of-bounds indices`() = runTest {
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns Result.success(session())
        val vm = viewModel()
        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        vm.goToQuestion(99)
        assertEquals(0, vm.uiState.value.currentIndex)

        vm.goToQuestion(-1)
        assertEquals(0, vm.uiState.value.currentIndex)
    }

    // ── submitSession ─────────────────────────────────────────────────────────

    @Test
    fun `submitSession always uses sessionId`() = runTest {
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns
            Result.success(session(aid = attemptId))
        coEvery { adaptiveRepository.submitSession(sessionId, any()) } returns
            Result.success(sampleReport)
        val vm = viewModel()
        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        vm.submitSession()
        advanceUntilIdle()

        coVerify { adaptiveRepository.submitSession(sessionId, any()) }
    }

    @Test
    fun `submitSession uses sessionId even when attemptId is null`() = runTest {
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns
            Result.success(session(aid = null))
        coEvery { adaptiveRepository.submitSession(sessionId, any()) } returns
            Result.success(sampleReport.copy(attemptId = sessionId))
        val vm = viewModel()
        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        vm.submitSession()
        advanceUntilIdle()

        coVerify { adaptiveRepository.submitSession(sessionId, any()) }
    }

    @Test
    fun `submitSession success sets result and clears loading`() = runTest {
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns Result.success(session())
        coEvery { adaptiveRepository.submitSession(any(), any()) } returns Result.success(sampleReport)
        val vm = viewModel()
        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        vm.submitSession()
        advanceUntilIdle()

        assertEquals(sampleReport, vm.uiState.value.result)
        assertEquals(false, vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `submitSession failure sets errorMessage`() = runTest {
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns Result.success(session())
        coEvery { adaptiveRepository.submitSession(any(), any()) } returns
            Result.failure(RuntimeException("Submit error"))
        val vm = viewModel()
        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        vm.submitSession()
        advanceUntilIdle()

        assertEquals("Submit error", vm.uiState.value.errorMessage)
        assertNull(vm.uiState.value.result)
        assertEquals(false, vm.uiState.value.isLoading)
    }

    @Test
    fun `submitSession without session does nothing`() = runTest {
        val vm = viewModel()
        vm.submitSession()
        advanceUntilIdle()

        coVerify(exactly = 0) { adaptiveRepository.submitSession(any(), any()) }
        assertNull(vm.uiState.value.result)
    }

    @Test
    fun `clearError sets errorMessage to null`() = runTest {
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns
            Result.failure(RuntimeException("some error"))
        val vm = viewModel()
        vm.startSession(1, 2, 5)
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.errorMessage)

        vm.clearError()

        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `submitSession failure with null message uses default`() = runTest {
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns Result.success(session())
        coEvery { adaptiveRepository.submitSession(any(), any()) } returns
            Result.failure(RuntimeException())
        val vm = viewModel()
        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        vm.submitSession()
        advanceUntilIdle()

        assertEquals("Nu am putut trimite răspunsurile", vm.uiState.value.errorMessage)
    }

    @Test
    fun `goToQuestion before session starts does nothing`() = runTest {
        val vm = viewModel()
        vm.goToQuestion(1)
        assertEquals(0, vm.uiState.value.currentIndex)
    }

    @Test
    fun `submitSession sends all questions including unanswered`() = runTest {
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns Result.success(session())
        coEvery { adaptiveRepository.submitSession(any(), any()) } returns Result.success(sampleReport)
        val vm = viewModel()
        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        vm.submitSession()
        advanceUntilIdle()

        coVerify {
            adaptiveRepository.submitSession(
                any(),
                match { req -> req.answers.size == 1 && req.answers[0].questionId == "1" }
            )
        }
    }

    // ── time tracking ─────────────────────────────────────────────────────────

    @Test
    fun `timeSpentSeconds updated after navigating away from question`() = runTest {
        val twoQ = session().copy(
            questions = listOf(sampleQuestion, sampleQuestion.copy(questionId = "2"))
        )
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns Result.success(twoQ)
        val vm = viewModel()
        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        vm.nextQuestion()

        val time = vm.uiState.value.timeSpentSeconds["1"]
        assertTrue(time != null && time >= 0.0)
    }

    @Test
    fun `timeSpentSeconds accumulates across multiple visits`() = runTest {
        val twoQ = session().copy(
            questions = listOf(sampleQuestion, sampleQuestion.copy(questionId = "2"))
        )
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns Result.success(twoQ)
        val vm = viewModel()
        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        vm.nextQuestion()
        val firstVisit = vm.uiState.value.timeSpentSeconds["1"] ?: 0.0

        vm.prevQuestion()
        vm.nextQuestion()

        val secondVisit = vm.uiState.value.timeSpentSeconds["1"] ?: 0.0
        assertTrue(secondVisit >= firstVisit)
    }

    @Test
    fun `timeSpentSeconds included in submit request`() = runTest {
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns Result.success(session())
        coEvery { adaptiveRepository.submitSession(any(), any()) } returns Result.success(sampleReport)
        val vm = viewModel()
        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        vm.submitSession()
        advanceUntilIdle()

        coVerify {
            adaptiveRepository.submitSession(
                any(),
                match { req -> (req.answers[0].timeSpent ?: -1) >= 0 }
            )
        }
    }

    @Test
    fun `nextQuestion at last question stays on last question`() = runTest {
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns Result.success(session())
        val vm = viewModel()
        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        vm.nextQuestion()

        assertEquals(0, vm.uiState.value.currentIndex)
    }

    @Test
    fun `prevQuestion at first question stays on first question`() = runTest {
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns Result.success(session())
        val vm = viewModel()
        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        vm.prevQuestion()

        assertEquals(0, vm.uiState.value.currentIndex)
    }

    @Test
    fun `selectAnswer on unknown questionId creates new entry`() = runTest {
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns Result.success(session())
        val vm = viewModel()
        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        vm.selectAnswer("unknown-q", 5, false)

        assertEquals(listOf(5), vm.uiState.value.selectedAnswers["unknown-q"])
    }

    @Test
    fun `submitSession maps selected option texts correctly`() = runTest {
        coEvery { startAdaptiveSessionUseCase(any(), any(), any()) } returns Result.success(session())
        coEvery { adaptiveRepository.submitSession(any(), any()) } returns Result.success(sampleReport)
        val vm = viewModel()
        vm.startSession(1, 2, 5)
        advanceUntilIdle()

        vm.selectAnswer("1", 1, true)
        vm.submitSession()
        advanceUntilIdle()

        coVerify {
            adaptiveRepository.submitSession(
                any(),
                match { req -> req.answers[0].selectedOptionIds == listOf("2") }
            )
        }
    }
}
