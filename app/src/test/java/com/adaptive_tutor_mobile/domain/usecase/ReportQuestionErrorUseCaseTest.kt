package com.adaptive_tutor_mobile.domain.usecase

import com.adaptive_tutor_mobile.domain.repository.test.ErrorReportRepository
import com.adaptive_tutor_mobile.domain.usecase.test.ReportQuestionErrorUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReportQuestionErrorUseCaseTest {

    private lateinit var repo: ErrorReportRepository
    private lateinit var useCase: ReportQuestionErrorUseCase

    @Before
    fun setup() {
        repo = mockk()
        useCase = ReportQuestionErrorUseCase(repo)
    }

    // ── validare client ──────────────────────────────────────────────────────

    @Test
    fun `fails when description is empty`() = runTest {
        val result = useCase.invoke(questionId = 1, description = "")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        coVerify(exactly = 0) { repo.reportError(any(), any()) }
    }

    @Test
    fun `fails when description is shorter than 10 characters`() = runTest {
        val result = useCase.invoke(questionId = 1, description = "scurt")

        assertTrue(result.isFailure)
        val msg = result.exceptionOrNull()?.message.orEmpty()
        assertTrue("Mesaj trebuie să menționeze minimul", msg.contains("10"))
        coVerify(exactly = 0) { repo.reportError(any(), any()) }
    }

    @Test
    fun `fails when description is exactly 9 characters`() = runTest {
        val result = useCase.invoke(questionId = 1, description = "123456789")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repo.reportError(any(), any()) }
    }

    @Test
    fun `fails when description exceeds 2000 characters`() = runTest {
        val tooLong = "a".repeat(2001)

        val result = useCase.invoke(questionId = 1, description = tooLong)

        assertTrue(result.isFailure)
        val msg = result.exceptionOrNull()?.message.orEmpty()
        assertTrue("Mesaj trebuie să menționeze maximul", msg.contains("2000"))
        coVerify(exactly = 0) { repo.reportError(any(), any()) }
    }

    @Test
    fun `trims whitespace and re-validates length`() = runTest {
        // "  hi  " are 6 caractere după trim — invalid
        val result = useCase.invoke(questionId = 1, description = "  hi  ")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repo.reportError(any(), any()) }
    }

    // ── boundary cases ───────────────────────────────────────────────────────

    @Test
    fun `accepts description with exactly 10 characters`() = runTest {
        coEvery { repo.reportError(any(), any()) } returns Result.success(Unit)

        val result = useCase.invoke(questionId = 1, description = "1234567890")

        assertTrue(result.isSuccess)
        coVerify { repo.reportError(1, "1234567890") }
    }

    @Test
    fun `accepts description with exactly 2000 characters`() = runTest {
        val exactlyMax = "a".repeat(2000)
        coEvery { repo.reportError(any(), any()) } returns Result.success(Unit)

        val result = useCase.invoke(questionId = 1, description = exactlyMax)

        assertTrue(result.isSuccess)
        coVerify { repo.reportError(1, exactlyMax) }
    }

    // ── delegare către repo ──────────────────────────────────────────────────

    @Test
    fun `passes trimmed description to repository on success`() = runTest {
        coEvery { repo.reportError(any(), any()) } returns Result.success(Unit)

        val result = useCase.invoke(
            questionId = 42,
            description = "   răspunsul corect este marcat greșit   "
        )

        assertTrue(result.isSuccess)
        coVerify {
            repo.reportError(42, "răspunsul corect este marcat greșit")
        }
    }

    @Test
    fun `propagates repository failure with original message`() = runTest {
        coEvery { repo.reportError(any(), any()) } returns
                Result.failure(IllegalStateException("Server down"))

        val result = useCase.invoke(questionId = 1, description = "destul de lung pentru validare")

        assertTrue(result.isFailure)
        assertEquals("Server down", result.exceptionOrNull()?.message)
    }
}