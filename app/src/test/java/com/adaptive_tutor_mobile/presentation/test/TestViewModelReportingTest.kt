package com.adaptive_tutor_mobile.presentation.test

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.adaptive_tutor_mobile.testing.MainDispatcherRule
import com.adaptive_tutor_mobile.data.remote.dto.StartAttemptResponseDto
import com.adaptive_tutor_mobile.domain.repository.test.TestRepository
import com.adaptive_tutor_mobile.domain.usecase.test.ReportQuestionErrorUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TestViewModelReportingTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: TestRepository
    private lateinit var reportUseCase: ReportQuestionErrorUseCase
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        repository = mockk()
        reportUseCase = mockk()
        savedStateHandle = SavedStateHandle()

        // startTest e apelat în init() — îl mockuim ca să nu blocheze
        coEvery { repository.startTest(any()) } returns Result.success(mockk(relaxed = true))
    }

    private fun vm(testId: String = "test-1") = TestViewModel(
        repository,
        reportUseCase,
        SavedStateHandle(mapOf("testId" to testId))
    )

    // ── showReportDialog ─────────────────────────────────────────────────────

    @Test
    fun `showReportDialog opens dialog with question id`() = runTest {
        val vm = vm()
        advanceUntilIdle()

        vm.showReportDialog(questionId = 42)

        val state = vm.state.value
        assertTrue(state.showReportDialog)
        assertEquals(42, state.reportingQuestionId)
        assertNull(state.reportError)
    }

    @Test
    fun `showReportDialog clears previous error when reopened`() = runTest {
        val vm = vm()
        advanceUntilIdle()
        // Simulează o eroare anterioară
        coEvery { reportUseCase(any(), any()) } returns
                Result.failure(IllegalStateException("Eroare veche"))
        vm.showReportDialog(1)
        vm.submitReport("destul de lung pentru validare")
        advanceUntilIdle()
        assertEquals("Eroare veche", vm.state.value.reportError)

        // Redeschide dialog pe altă întrebare
        vm.showReportDialog(2)

        val state = vm.state.value
        assertEquals(2, state.reportingQuestionId)
        assertNull(state.reportError)
    }

    // ── dismissReportDialog ──────────────────────────────────────────────────

    @Test
    fun `dismissReportDialog resets dialog state`() = runTest {
        val vm = vm()
        advanceUntilIdle()
        vm.showReportDialog(5)

        vm.dismissReportDialog()

        val state = vm.state.value
        assertFalse(state.showReportDialog)
        assertNull(state.reportingQuestionId)
        assertNull(state.reportError)
        assertFalse(state.isSubmittingReport)
    }

    // ── submitReport ─────────────────────────────────────────────────────────

    @Test
    fun `submitReport does nothing if no question is being reported`() = runTest {
        val vm = vm()
        advanceUntilIdle()

        vm.submitReport("descriere valida foarte lunga")
        advanceUntilIdle()

        coVerify(exactly = 0) { reportUseCase(any(), any()) }
    }

    @Test
    fun `submitReport success closes dialog and sets reportSuccess`() = runTest {
        coEvery { reportUseCase(7, "descriere valida foarte lunga") } returns Result.success(Unit)
        val vm = vm()
        advanceUntilIdle()
        vm.showReportDialog(7)

        vm.submitReport("descriere valida foarte lunga")
        advanceUntilIdle()

        val state = vm.state.value
        assertFalse(state.showReportDialog)
        assertNull(state.reportingQuestionId)
        assertFalse(state.isSubmittingReport)
        assertEquals("Raportul a fost trimis. Mulțumim!", state.reportSuccess)
    }

    @Test
    fun `submitReport failure keeps dialog open with error message`() = runTest {
        coEvery { reportUseCase(7, any()) } returns
                Result.failure(IllegalStateException("Descrierea e prea scurtă"))
        val vm = vm()
        advanceUntilIdle()
        vm.showReportDialog(7)

        vm.submitReport("ceva")
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue("Dialogul rămâne deschis pentru retry", state.showReportDialog)
        assertEquals(7, state.reportingQuestionId)
        assertFalse(state.isSubmittingReport)
        assertEquals("Descrierea e prea scurtă", state.reportError)
        assertNull(state.reportSuccess)
    }

    @Test
    fun `submitReport failure with null message uses fallback`() = runTest {
        coEvery { reportUseCase(any(), any()) } returns
                Result.failure(RuntimeException())
        val vm = vm()
        advanceUntilIdle()
        vm.showReportDialog(1)

        vm.submitReport("descriere suficient de lunga")
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals("Eroare la trimiterea raportului", state.reportError)
    }

    @Test
    fun `submitReport transitions through isSubmittingReport flag`() = runTest {
        coEvery { reportUseCase(any(), any()) } returns Result.success(Unit)
        val vm = vm()
        advanceUntilIdle()
        vm.showReportDialog(1)

        vm.state.test {
            // skip starea curentă cu dialog deschis
            assertTrue(awaitItem().showReportDialog)

            vm.submitReport("descriere suficient de lunga")

            // emit cu isSubmittingReport = true
            val submitting = awaitItem()
            assertTrue(submitting.isSubmittingReport)
            assertNull(submitting.reportError)

            // emit final cu success
            val done = awaitItem()
            assertFalse(done.isSubmittingReport)
            assertFalse(done.showReportDialog)
            assertEquals("Raportul a fost trimis. Mulțumim!", done.reportSuccess)
        }
    }

    // ── clearReportSuccess ───────────────────────────────────────────────────

    @Test
    fun `clearReportSuccess removes the success message`() = runTest {
        coEvery { reportUseCase(any(), any()) } returns Result.success(Unit)
        val vm = vm()
        advanceUntilIdle()
        vm.showReportDialog(1)
        vm.submitReport("descriere suficient de lunga")
        advanceUntilIdle()
        assertEquals("Raportul a fost trimis. Mulțumim!", vm.state.value.reportSuccess)

        vm.clearReportSuccess()

        assertNull(vm.state.value.reportSuccess)
    }
}