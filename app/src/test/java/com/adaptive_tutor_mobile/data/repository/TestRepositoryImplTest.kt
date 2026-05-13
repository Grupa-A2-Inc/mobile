package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.TestApi
import com.adaptive_tutor_mobile.data.remote.dto.AttemptReportDTO
import com.adaptive_tutor_mobile.data.remote.dto.QuestionForAttemptReportDTO
import com.adaptive_tutor_mobile.data.remote.dto.StartAttemptResponseDto
import com.adaptive_tutor_mobile.data.remote.dto.SubmitAnswerDto
import com.adaptive_tutor_mobile.data.remote.dto.SubmitRequestDto
import com.adaptive_tutor_mobile.data.remote.dto.TestInfoForAttemptDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestRepositoryImplTest {

    private lateinit var api: TestApi
    private lateinit var repository: TestRepositoryImpl

    private val testId = "test-id-123"
    private val attemptId = "attempt-id-456"

    @Before
    fun setup() {
        api = mockk()
        repository = TestRepositoryImpl(api)
    }

    // ── startTest ─────────────────────────────────────────────────────────────

    @Test
    fun `startTest returns success with response`() = runTest {
        val response = StartAttemptResponseDto(
            attemptId = attemptId,
            attemptNumber = 1,
            startedAt = "2026-01-01T10:00:00Z",
            timeLimitSec = 600,
            test = TestInfoForAttemptDto(id = testId, title = "Test"),
            questions = emptyList()
        )
        coEvery { api.startTest(testId) } returns response

        val result = repository.startTest(testId)

        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        assertEquals(attemptId, result.getOrNull()?.attemptId)
    }

    @Test
    fun `startTest returns failure on exception`() = runTest {
        coEvery { api.startTest(any()) } throws RuntimeException("Network error")

        val result = repository.startTest(testId)

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `startTest returns failure on HTTP exception`() = runTest {
        coEvery { api.startTest(any()) } throws IllegalStateException("HTTP 404 Not Found")

        val result = repository.startTest(testId)

        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    // ── submitAttempt ─────────────────────────────────────────────────────────

    @Test
    fun `submitAttempt returns success with report`() = runTest {
        val report = AttemptReportDTO(
            attemptId = attemptId,
            score = 90.0,
            scorePercent = 90.0,
            passed = true,
            completedAt = "2026-01-01T10:30:00Z",
            questions = listOf(
                QuestionForAttemptReportDTO(
                    questionId = 1,
                    questionType = "SINGLE_CHOICE",
                    content = "Q1",
                    selectedOptionIds = listOf(2),
                    correctOptionIds = listOf(2),
                    correct = true
                )
            )
        )
        val request = SubmitRequestDto(
            answers = listOf(
                SubmitAnswerDto(questionId = 1, selectedOptionIds = listOf(2), timeSpent = 5.0)
            )
        )
        coEvery { api.submitAttempt(attemptId, request) } returns report

        val result = repository.submitAttempt(attemptId, request)

        assertTrue(result.isSuccess)
        assertEquals(report, result.getOrNull())
        assertEquals(true, result.getOrNull()?.passed)
        assertEquals(90.0, result.getOrNull()?.scorePercent)
    }

    @Test
    fun `submitAttempt returns failure on exception`() = runTest {
        val request = SubmitRequestDto(answers = emptyList())
        coEvery { api.submitAttempt(any(), any()) } throws RuntimeException("403 Forbidden")

        val result = repository.submitAttempt(attemptId, request)

        assertTrue(result.isFailure)
        assertEquals("403 Forbidden", result.exceptionOrNull()?.message)
    }

    @Test
    fun `submitAttempt with empty answers list returns success`() = runTest {
        val report = AttemptReportDTO(
            attemptId = attemptId, score = 0.0, scorePercent = 0.0,
            passed = false, completedAt = null, questions = emptyList()
        )
        val request = SubmitRequestDto(answers = emptyList())
        coEvery { api.submitAttempt(attemptId, request) } returns report

        val result = repository.submitAttempt(attemptId, request)

        assertTrue(result.isSuccess)
        assertEquals(false, result.getOrNull()?.passed)
    }

    @Test
    fun `submitAttempt returns failure on network timeout`() = runTest {
        val request = SubmitRequestDto(answers = emptyList())
        coEvery { api.submitAttempt(any(), any()) } throws java.net.SocketTimeoutException("timeout")

        val result = repository.submitAttempt(attemptId, request)

        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }
}
