package com.adaptive_tutor_mobile.presentation.auth

import com.adaptive_tutor_mobile.domain.usecase.auth.LogoutUseCase
import com.adaptive_tutor_mobile.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RoleBlockedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val logoutUseCase: LogoutUseCase = mockk()

    @Test
    fun `logout clears session flow and sets navigateToLogin on success`() = runTest {
        coEvery { logoutUseCase() } returns Result.success(Unit)
        val viewModel = RoleBlockedViewModel(logoutUseCase)

        viewModel.logout()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.navigateToLogin)
        assertFalse(viewModel.uiState.value.isLoggingOut)
        assertEquals(null, viewModel.uiState.value.errorMessage)
        coVerify(exactly = 1) { logoutUseCase() }
    }

    @Test
    fun `logout exposes repository error and keeps user on blocked screen`() = runTest {
        coEvery { logoutUseCase() } returns Result.failure(IllegalStateException("Logout failed"))
        val viewModel = RoleBlockedViewModel(logoutUseCase)

        viewModel.logout()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.navigateToLogin)
        assertFalse(viewModel.uiState.value.isLoggingOut)
        assertEquals("Logout failed", viewModel.uiState.value.errorMessage)
    }
}
