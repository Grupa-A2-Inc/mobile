package com.adaptive_tutor_mobile.presentation.test

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.adaptive_tutor_mobile.domain.model.test.AttemptHistory
import com.adaptive_tutor_mobile.domain.model.test.BestAttempt
import com.adaptive_tutor_mobile.domain.usecase.test.GetTestAttemptsUseCase
import com.adaptive_tutor_mobile.domain.usecase.test.TestAttemptsResult
import com.adaptive_tutor_mobile.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class TestAttemptsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getTestAttemptsUseCase = mockk<GetTestAttemptsUseCase>()
    private val testId = "test-456"
    private val savedStateHandle = SavedStateHandle(mapOf("testId" to testId))

    private lateinit var viewModel: TestAttemptsViewModel

    @Test
    fun `init should load attempts and emit sorted success state`() = runTest {
        // Arrange
        val date1 = "2023-10-01T10:00:00Z"
        val date2 = "2023-10-01T12:00:00Z"
        val date3 = "2023-10-01T11:00:00Z"
        
        val attempts = listOf(
            AttemptHistory(attemptId = "1", attemptNumber = 1, score = 7.0, scorePercent = 70.0, passed = true, date = date1, status = "PASSED"),
            AttemptHistory(attemptId = "2", attemptNumber = 2, score = 9.0, scorePercent = 90.0, passed = true, date = date2, status = "PASSED"),
            AttemptHistory(attemptId = "3", attemptNumber = 3, score = 8.0, scorePercent = 80.0, passed = true, date = date3, status = "PASSED")
        )
        val bestAttempt = BestAttempt(attemptId = "2", score = 9.0, scorePercent = 90.0, date = date2)
        val resultData = TestAttemptsResult(attempts = attempts, bestAttempt = bestAttempt)
        
        coEvery { getTestAttemptsUseCase(testId) } returns Result.success(resultData)

        // Act
        viewModel = TestAttemptsViewModel(savedStateHandle, getTestAttemptsUseCase)

        // Assert
        viewModel.uiState.test {
            // Initial/Loading states
            var state = awaitItem()
            while (state.isLoading || state.attempts.isEmpty()) {
                state = awaitItem()
                if (state.attempts.isNotEmpty()) break
            }
            
            // Verify sorting (Descending by date: date2 > date3 > date1)
            assertEquals(3, state.attempts.size)
            assertEquals("2", state.attempts[0].attemptId)
            assertEquals("3", state.attempts[1].attemptId)
            assertEquals("1", state.attempts[2].attemptId)
            
            assertEquals(bestAttempt, state.bestAttempt)
            assertFalse(state.isLoading)
            assertNull(state.error)
        }
    }

    @Test
    fun `init should emit error state when use case fails`() = runTest {
        // Arrange
        val errorMessage = "Failed to load"
        coEvery { getTestAttemptsUseCase(testId) } returns Result.failure(Exception(errorMessage))

        // Act
        viewModel = TestAttemptsViewModel(savedStateHandle, getTestAttemptsUseCase)

        // Assert
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading || state.error == null) {
                state = awaitItem()
                if (state.error != null) break
            }
            
            assertEquals(errorMessage, state.error)
            assertFalse(state.isLoading)
            assertTrue(state.attempts.isEmpty())
        }
    }

    @Test
    fun `loadAttempts should refresh state`() = runTest {
        // Arrange
        val attempts = listOf(
            AttemptHistory(attemptId = "1", attemptNumber = 1, score = 7.0, scorePercent = 70.0, passed = true, date = "2023-10-01", status = "PASSED")
        )
        val resultData = TestAttemptsResult(attempts = attempts, bestAttempt = null)
        coEvery { getTestAttemptsUseCase(testId) } returns Result.success(resultData)
        
        viewModel = TestAttemptsViewModel(savedStateHandle, getTestAttemptsUseCase)
        
        viewModel.uiState.test {
            // Skip init cycle
            var state = awaitItem()
            while (state.isLoading || state.attempts.isEmpty()) {
                state = awaitItem()
                if (state.attempts.isNotEmpty()) break
            }
            
            // Act
            viewModel.loadAttempts()
            
            // Assert
            assertTrue(awaitItem().isLoading)
            val successState = awaitItem()
            assertEquals(1, successState.attempts.size)
            assertFalse(successState.isLoading)
        }
    }

    @Test
    fun `init throws when testId missing from SavedStateHandle`() {
        val emptySavedStateHandle = SavedStateHandle()

        var thrown = false
        try {
            TestAttemptsViewModel(emptySavedStateHandle, getTestAttemptsUseCase)
        } catch (e: IllegalStateException) {
            thrown = true
        }
        assertTrue(thrown)
    }

    @Test
    fun `loadAttempts uses fallback error message when localizedMessage is null`() = runTest {
        coEvery { getTestAttemptsUseCase(testId) } returns Result.failure(object : Throwable() {
            override val message: String? = null
        })

        viewModel = TestAttemptsViewModel(savedStateHandle, getTestAttemptsUseCase)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading || (state.error == null && state.attempts.isEmpty())) {
                state = awaitItem()
                if (state.error != null) break
            }
            assertEquals("Eroare la încărcarea încercărilor", state.error)
        }
    }
}
