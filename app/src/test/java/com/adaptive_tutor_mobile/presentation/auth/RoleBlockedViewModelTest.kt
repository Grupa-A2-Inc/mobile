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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RoleBlockedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val logoutUseCase: LogoutUseCase = mockk()

    @Test
    fun `logout triggers navigation to login`() = runTest {
        coEvery { logoutUseCase() } returns Result.success(Unit)

        val viewModel = RoleBlockedViewModel(logoutUseCase)

        viewModel.logout()
        advanceUntilIdle()

        assertFalse(viewModel.isLoggingOut.value)
        assertTrue(viewModel.navigateToLogin.value)
        coVerify { logoutUseCase() }
    }

    @Test
    fun `onNavigatedToLogin resets navigation flag`() = runTest {
        coEvery { logoutUseCase() } returns Result.success(Unit)

        val viewModel = RoleBlockedViewModel(logoutUseCase)
        viewModel.logout()
        advanceUntilIdle()

        viewModel.onNavigatedToLogin()

        assertFalse(viewModel.navigateToLogin.value)
    }
}
