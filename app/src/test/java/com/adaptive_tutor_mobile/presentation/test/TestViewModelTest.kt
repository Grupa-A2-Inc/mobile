package com.adaptive_tutor_mobile.presentation.test

import androidx.lifecycle.SavedStateHandle
import com.adaptive_tutor_mobile.data.remote.dto.AttemptReportDTO
import com.adaptive_tutor_mobile.data.remote.dto.OptionForStudentDto
import com.adaptive_tutor_mobile.data.remote.dto.QuestionForStudentDto
import com.adaptive_tutor_mobile.data.remote.dto.StartAttemptResponseDto
import com.adaptive_tutor_mobile.data.remote.dto.SubmitRequestDto
import com.adaptive_tutor_mobile.data.remote.dto.TestInfoForAttemptDto
import com.adaptive_tutor_mobile.domain.repository.TestRepository
import com.adaptive_tutor_mobile.domain.usecase.ReportQuestionErrorUseCase
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TestViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: TestRepository
    private lateinit var reportUseCase: ReportQuestionErrorUseCase

    private val testId = "test-uuid-123"
    private val attemptId = "attempt-uuid-456"

    private val sampleOptions = listOf(
        OptionForStudentDto(optionId = 10, text = "3", displayOrder = 0),
        OptionForStudentDto(optionId = 11, text = "4", displayOrder = 1)
    )

    private val sampleQuestion = QuestionForStudentDto(
        questionId = 1,
        questionType = "SINGLE_CHOICE",
        content = "What is 2+2?",
        difficulty = 0.5,
        options = sampleOptions
    )

    private val sampleStartResponse = StartAttemptResponseDto(
        attemptId = attemptId,
        attemptNumber = 1,
        startedAt = "2026-01-01T10:00:00Z",
        timeLimitSec = null,
        test = TestInfoForAttemptDto(id = testId, title = "Sample Test"),
        questions = listOf(sampleQuestion)
    )

    @Before
    fun setup() {
        repository = mockk()
        reportUseCase = mockk(relaxed = true)
    }

    private fun viewModel(id: String? = testId) = TestViewModel(
        repository = repository,
        reportQuestionErrorUseCase = reportUseCase,
        savedStateHandle = SavedStateHandle(
            if (id != null) mapOf("testId" to id) else emptyMap()
        )
    )

    // ── init ─────────────────────────────────────────────────────────────────

    @Test
    fun `init with testId calls startTest and sets questions on success`() = runTest {
        coEvery { repository.startTest(testId) } returns Result.success(sampleStartResponse)

        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(attemptId, vm.state.value.attemptId)
        assertEquals(listOf(sampleQuestion), vm.state.value.questions)
        assertNull(vm.state.value.error)
        assertEquals(false, vm.state.value.isLoading)
    }

    @Test
    fun `init without testId does not call repository and leaves state empty`() = runTest {
        val vm = viewModel(id = null)
        advanceUntilIdle()

        assertTrue(vm.state.value.questions.isEmpty())
        assertNull(vm.state.value.attemptId)
        coVerify(exactly = 0) { repository.startTest(any()) }
    }

    @Test
    fun `startTest failure sets error and clears loading`() = runTest {
        coEvery { repository.startTest(testId) } returns
                Result.failure(RuntimeException("Network error"))

        val vm = viewModel()
        advanceUntilIdle()

        assertEquals("Network error", vm.state.value.error)
        assertNull(vm.state.value.attemptId)
        assertEquals(false, vm.state.value.isLoading)
    }

    @Test
    fun `startTest failure with null message falls back to generic message`() = runTest {
        coEvery { repository.startTest(testId) } returns
                Result.failure(RuntimeException())

        val vm = viewModel()
        advanceUntilIdle()

        assertEquals("Eroare la pornirea testului", vm.state.value.error)
    }

    // ── selectOption ─────────────────────────────────────────────────────────

    @Test
    fun `selectOption single choice replaces existing selection`() = runTest {
        coEvery { repository.startTest(testId) } returns Result.success(sampleStartResponse)
        val vm = viewModel()
        advanceUntilIdle()

        vm.selectOption(questionId = 1, optionId = 10, singleChoice = true)
        vm.selectOption(questionId = 1, optionId = 11, singleChoice = true)

        assertEquals(listOf(11), vm.state.value.selectedAnswers[1])
    }

    @Test
    fun `selectOption multi choice adds options`() = runTest {
        coEvery { repository.startTest(testId) } returns Result.success(sampleStartResponse)
        val vm = viewModel()
        advanceUntilIdle()

        vm.selectOption(questionId = 1, optionId = 10, singleChoice = false)
        vm.selectOption(questionId = 1, optionId = 11, singleChoice = false)

        val selected = vm.state.value.selectedAnswers[1]
        assertTrue(selected!!.contains(10))
        assertTrue(selected.contains(11))
    }

    @Test
    fun `selectOption multi choice removes already selected option`() = runTest {
        coEvery { repository.startTest(testId) } returns Result.success(sampleStartResponse)
        val vm = viewModel()
        advanceUntilIdle()

        vm.selectOption(questionId = 1, optionId = 10, singleChoice = false)
        vm.selectOption(questionId = 1, optionId = 11, singleChoice = false)
        vm.selectOption(questionId = 1, optionId = 10, singleChoice = false) // deselect

        assertEquals(listOf(11), vm.state.value.selectedAnswers[1])
    }

    // ── navigation ────────────────────────────────────────────────────────────

    @Test
    fun `nextQuestion increments currentIndex`() = runTest {
        val twoQuestions = sampleStartResponse.copy(
            questions = listOf(sampleQuestion, sampleQuestion.copy(questionId = 2))
        )
        coEvery { repository.startTest(testId) } returns Result.success(twoQuestions)
        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(0, vm.state.value.currentIndex)
        vm.nextQuestion()
        assertEquals(1, vm.state.value.currentIndex)
    }

    @Test
    fun `nextQuestion does not exceed last question`() = runTest {
        coEvery { repository.startTest(testId) } returns Result.success(sampleStartResponse)
        val vm = viewModel()
        advanceUntilIdle()

        vm.nextQuestion()
        assertEquals(0, vm.state.value.currentIndex)
    }

    @Test
    fun `prevQuestion decrements currentIndex`() = runTest {
        val twoQuestions = sampleStartResponse.copy(
            questions = listOf(sampleQuestion, sampleQuestion.copy(questionId = 2))
        )
        coEvery { repository.startTest(testId) } returns Result.success(twoQuestions)
        val vm = viewModel()
        advanceUntilIdle()

        vm.nextQuestion()
        vm.prevQuestion()
        assertEquals(0, vm.state.value.currentIndex)
    }

    @Test
    fun `prevQuestion does not go below zero`() = runTest {
        coEvery { repository.startTest(testId) } returns Result.success(sampleStartResponse)
        val vm = viewModel()
        advanceUntilIdle()

        vm.prevQuestion()
        assertEquals(0, vm.state.value.currentIndex)
    }

    @Test
    fun `goToQuestion sets index within valid range`() = runTest {
        val twoQuestions = sampleStartResponse.copy(
            questions = listOf(sampleQuestion, sampleQuestion.copy(questionId = 2))
        )
        coEvery { repository.startTest(testId) } returns Result.success(twoQuestions)
        val vm = viewModel()
        advanceUntilIdle()

        vm.goToQuestion(1)
        assertEquals(1, vm.state.value.currentIndex)
    }

    @Test
    fun `goToQuestion ignores out-of-bounds index`() = runTest {
        coEvery { repository.startTest(testId) } returns Result.success(sampleStartResponse)
        val vm = viewModel()
        advanceUntilIdle()

        vm.goToQuestion(99)
        assertEquals(0, vm.state.value.currentIndex)

        vm.goToQuestion(-1)
        assertEquals(0, vm.state.value.currentIndex)
    }

    // ── submit ────────────────────────────────────────────────────────────────

    @Test
    fun `submitTest with null attemptId does nothing`() = runTest {
        coEvery { repository.startTest(testId) } returns
                Result.failure(RuntimeException("fail"))
        val vm = viewModel()
        advanceUntilIdle()

        vm.submitTest()
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.submitAttempt(any(), any()) }
    }

    @Test
    fun `submitTest success sets report and clears loading`() = runTest {
        val report = AttemptReportDTO(
            attemptId = attemptId, score = 80.0, scorePercent = 80.0,
            passed = true, completedAt = "2026-01-01T10:30:00Z", questions = emptyList()
        )
        coEvery { repository.startTest(testId) } returns Result.success(sampleStartResponse)
        coEvery { repository.submitAttempt(attemptId, any()) } returns Result.success(report)

        val vm = viewModel()
        advanceUntilIdle()
        vm.submitTest()
        advanceUntilIdle()

        assertEquals(report, vm.state.value.report)
        assertNull(vm.state.value.error)
        assertEquals(false, vm.state.value.isLoading)
    }

    @Test
    fun `submitTest failure sets error`() = runTest {
        coEvery { repository.startTest(testId) } returns Result.success(sampleStartResponse)
        coEvery { repository.submitAttempt(attemptId, any()) } returns
                Result.failure(RuntimeException("Submit failed"))

        val vm = viewModel()
        advanceUntilIdle()
        vm.submitTest()
        advanceUntilIdle()

        assertEquals("Submit failed", vm.state.value.error)
        assertNull(vm.state.value.report)
        assertEquals(false, vm.state.value.isLoading)
    }

    @Test
    fun `submitTest sends correct answers for all questions`() = runTest {
        coEvery { repository.startTest(testId) } returns Result.success(sampleStartResponse)
        coEvery { repository.submitAttempt(attemptId, any()) } returns Result.success(
            AttemptReportDTO(attemptId, 100.0, 100.0, true, null, emptyList())
        )

        val vm = viewModel()
        advanceUntilIdle()
        vm.selectOption(1, 11, true)
        vm.submitTest()
        advanceUntilIdle()

        coVerify {
            repository.submitAttempt(
                attemptId,
                match { req: SubmitRequestDto ->
                    req.answers.size == 1 &&
                            req.answers[0].questionId == 1 &&
                            req.answers[0].selectedOptionIds == listOf(11) &&
                            req.answers[0].timeSpent != null
                }
            )
        }
    }

    @Test
    fun `submitTest sends empty selectedOptionIds for unanswered questions`() = runTest {
        coEvery { repository.startTest(testId) } returns Result.success(sampleStartResponse)
        coEvery { repository.submitAttempt(attemptId, any()) } returns Result.success(
            AttemptReportDTO(attemptId, 0.0, 0.0, false, null, emptyList())
        )

        val vm = viewModel()
        advanceUntilIdle()
        // no selectOption call — questions unanswered
        vm.submitTest()
        advanceUntilIdle()

        coVerify {
            repository.submitAttempt(
                attemptId,
                match { req: SubmitRequestDto ->
                    req.answers[0].selectedOptionIds.isEmpty()
                }
            )
        }
    }
}