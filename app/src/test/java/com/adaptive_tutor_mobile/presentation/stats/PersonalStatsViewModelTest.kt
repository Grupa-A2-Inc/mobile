package com.adaptive_tutor_mobile.presentation.stats

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.adaptive_tutor_mobile.domain.model.CourseStats
import com.adaptive_tutor_mobile.domain.usecase.GetCourseStatsUseCase
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

@OptIn(ExperimentalCoroutinesApi::class)
class PersonalStatsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCourseStatsUseCase = mockk<GetCourseStatsUseCase>()
    private val courseId = "course-123"
    private val savedStateHandle = SavedStateHandle(mapOf("courseId" to courseId))

    private lateinit var viewModel: PersonalStatsViewModel

    @Test
    fun `init should load stats and emit success state`() = runTest {
        // Arrange
        val expectedStats = CourseStats(
            courseTitle = "Kotlin Advanced",
            totalTests = 10,
            totalTestDone = 5,
            passedTests = 4,
            bestScore = 95f,
            lowestScore = 60f,
            avgScore = 80f,
            hardestLessons = listOf("Coroutines", "Flow"),
            lastAttempts = emptyList()
        )
        coEvery { getCourseStatsUseCase(courseId) } returns Result.success(expectedStats)

        // Act
        viewModel = PersonalStatsViewModel(savedStateHandle, getCourseStatsUseCase)

        // Assert
        viewModel.uiState.test {
            // Initial state is emitted by StateFlow (isLoading=false initially in the data class)
            // But loadStats is called in init, so we might see multiple states
            val firstState = awaitItem()
            // In init, loadStats is called, which updates isLoading = true
            // Depending on how fast it runs, we might skip the very first default state
            
            if (firstState.isLoading) {
                val successState = awaitItem()
                assertEquals(expectedStats, successState.stats)
                assertFalse(successState.isLoading)
                assertNull(successState.error)
            } else {
                // If we got the initial default state first
                val loadingState = awaitItem()
                assertTrue(loadingState.isLoading)
                val successState = awaitItem()
                assertEquals(expectedStats, successState.stats)
                assertFalse(successState.isLoading)
                assertNull(successState.error)
            }
        }
    }

    @Test
    fun `init should load stats and emit error state when use case fails`() = runTest {
        // Arrange
        val errorMessage = "Network Error"
        coEvery { getCourseStatsUseCase(courseId) } returns Result.failure(Exception(errorMessage))

        // Act
        viewModel = PersonalStatsViewModel(savedStateHandle, getCourseStatsUseCase)

        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            if (state.isLoading) {
                val errorState = awaitItem()
                assertEquals(errorMessage, errorState.error)
                assertFalse(errorState.isLoading)
            } else {
                val loadingState = awaitItem()
                assertTrue(loadingState.isLoading)
                val errorState = awaitItem()
                assertEquals(errorMessage, errorState.error)
                assertFalse(errorState.isLoading)
            }
        }
    }

    @Test
    fun `loadStats should retry loading and emit success`() = runTest {
        // Arrange
        val expectedStats = CourseStats(
            courseTitle = "Kotlin Advanced",
            totalTests = 10,
            totalTestDone = 5,
            passedTests = 4,
            bestScore = 95f,
            lowestScore = 60f,
            avgScore = 80f,
            hardestLessons = listOf("Coroutines", "Flow"),
            lastAttempts = emptyList()
        )
        coEvery { getCourseStatsUseCase(courseId) } returns Result.success(expectedStats)
        
        // Initialize VM (this will call loadStats once)
        viewModel = PersonalStatsViewModel(savedStateHandle, getCourseStatsUseCase)
        
        // Assert
        viewModel.uiState.test {
            // 1. Consume the states from the 'init' call
            // Initial state
            var state = awaitItem()
            // If it's already loading or success, wait until it's finished
            while (state.isLoading || state.stats == null) {
                state = awaitItem()
                if (state.stats != null && !state.isLoading) break
            }
            
            // 2. Act: Trigger loadStats manually
            viewModel.loadStats()

            // 3. Assert: Verify the new cycle
            assertTrue(awaitItem().isLoading)
            val successState = awaitItem()
            assertEquals(expectedStats, successState.stats)
            assertFalse(successState.isLoading)
        }
    }
}
