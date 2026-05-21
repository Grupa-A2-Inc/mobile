package com.adaptive_tutor_mobile.ui.theme

import com.adaptive_tutor_mobile.di.SessionStore
import com.adaptive_tutor_mobile.testing.MainDispatcherRule
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.Test

class ThemeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionStore = mockk<SessionStore>()

    @Test
    fun `themeMode exposes current session store value`() = runTest {
        every { sessionStore.getThemeModeFlow() } returns MutableStateFlow("dark")

        val viewModel = ThemeViewModel(sessionStore)
        advanceUntilIdle()

        assertEquals("dark", viewModel.themeMode.value)
    }

    @Test
    fun `setThemeMode delegates to session store`() = runTest {
        every { sessionStore.getThemeModeFlow() } returns MutableStateFlow("system")
        coJustRun { sessionStore.saveThemeMode("light") }

        val viewModel = ThemeViewModel(sessionStore)
        viewModel.setThemeMode("light")
        advanceUntilIdle()

        coVerify(exactly = 1) { sessionStore.saveThemeMode("light") }
    }
}
