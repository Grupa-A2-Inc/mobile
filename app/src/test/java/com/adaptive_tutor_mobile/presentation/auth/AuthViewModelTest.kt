package com.adaptive_tutor_mobile.presentation.auth

import app.cash.turbine.test
import com.adaptive_tutor_mobile.data.remote.dto.RegisterRequest
import com.adaptive_tutor_mobile.di.SessionStore
import com.adaptive_tutor_mobile.domain.model.User
import com.adaptive_tutor_mobile.domain.model.UserRole
import com.adaptive_tutor_mobile.domain.usecase.ForgotPasswordUseCase
import com.adaptive_tutor_mobile.domain.usecase.LoginUseCase
import com.adaptive_tutor_mobile.domain.usecase.LogoutUseCase
import com.adaptive_tutor_mobile.domain.usecase.RegisterUseCase
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
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var registerUseCase: RegisterUseCase
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var forgotPasswordUseCase: ForgotPasswordUseCase
    private lateinit var sessionStore: SessionStore

    private val sampleUser = User(
            id = "user-1", firstName = "Ana", lastName = "Pop",
            email = "ana@x.com", role = UserRole.STUDENT, status = "ACTIVE",
            organizationId = null, organizationName = null
    )

    @Before
    fun setup() {
        loginUseCase = mockk()
        registerUseCase = mockk()
        logoutUseCase = mockk()
        forgotPasswordUseCase = mockk()
        sessionStore = mockk {
            coEvery { getUser() } returns null
        }
    }

    private fun viewModel() = AuthViewModel(
            loginUseCase, registerUseCase, logoutUseCase, forgotPasswordUseCase, sessionStore
            )

    // ── init ─────────────────────────────────────────────────────────────────

    @Test
    fun `init loads existing user from session store`() = runTest {
        coEvery { sessionStore.getUser() } returns sampleUser

        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(sampleUser, vm.currentUser.value)
    }

    @Test
    fun `init leaves currentUser null when session is empty`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        assertNull(vm.currentUser.value)
    }

    @Test
    fun `initial uiState is Idle`() = runTest {
        val vm = viewModel()
        assertEquals(AuthUiState.Idle, vm.uiState.value)
    }

    // ── login ────────────────────────────────────────────────────────────────

    @Test
    fun `login transitions Idle to Loading to Success`() = runTest {
        coEvery { loginUseCase("ana@x.com", "pwd") } returns Result.success(sampleUser)
        val vm = viewModel()
        advanceUntilIdle()

        vm.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            vm.login("ana@x.com", "pwd")

            assertEquals(AuthUiState.Loading, awaitItem())
            val success = awaitItem()
            assertTrue(success is AuthUiState.Success)
            assertEquals(sampleUser, (success as AuthUiState.Success).user)
        }
        assertEquals(sampleUser, vm.currentUser.value)
    }

    @Test
    fun `login failure emits Error with exception message`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns
        Result.failure(IllegalStateException("Bad credentials"))
        val vm = viewModel()
        advanceUntilIdle()

        vm.login("a@b.c", "wrong")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals("Bad credentials", (state as AuthUiState.Error).message)
    }

    @Test
    fun `login failure with null exception message falls back to default`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns
        Result.failure(RuntimeException())
        val vm = viewModel()
        advanceUntilIdle()

        vm.login("a@b.c", "x")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals("Eroare necunoscută", (state as AuthUiState.Error).message)
    }

    // ── register ─────────────────────────────────────────────────────────────

    @Test
    fun `register success emits Success and updates currentUser`() = runTest {
        val request = RegisterRequest(
                "Ana", "Pop", "ana@x.com", "password123", "password123",
                "Org", "RO", "Iași", "SCHOOL"
        )
        coEvery { registerUseCase(request) } returns Result.success(sampleUser)
        val vm = viewModel()
        advanceUntilIdle()

        vm.register(request)
        advanceUntilIdle()

        assertTrue(vm.uiState.value is AuthUiState.Success)
        assertEquals(sampleUser, vm.currentUser.value)
    }

    @Test
    fun `register failure emits Error`() = runTest {
        val request = RegisterRequest(
                "A", "B", "a@b.c", "12345678", "12345678",
                "Org", "RO", "Iași", "SCHOOL"
        )
        coEvery { registerUseCase(request) } returns
        Result.failure(IllegalStateException("Email already exists"))
        val vm = viewModel()
        advanceUntilIdle()

        vm.register(request)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals("Email already exists", (state as AuthUiState.Error).message)
    }

    // ── forgot password ──────────────────────────────────────────────────────

    @Test
    fun `forgotPassword success emits ForgotPasswordSent`() = runTest {
        coEvery { forgotPasswordUseCase("a@b.c") } returns Result.success(Unit)
        val vm = viewModel()
        advanceUntilIdle()

        vm.forgotPassword("a@b.c")
        advanceUntilIdle()

        assertEquals(AuthUiState.ForgotPasswordSent, vm.uiState.value)
    }

    @Test
    fun `forgotPassword failure emits Error`() = runTest {
        coEvery { forgotPasswordUseCase(any()) } returns
        Result.failure(IllegalStateException("Network down"))
        val vm = viewModel()
        advanceUntilIdle()

        vm.forgotPassword("a@b.c")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals("Network down", (state as AuthUiState.Error).message)
    }

    // ── logout ───────────────────────────────────────────────────────────────

    @Test
    fun `logout clears currentUser and returns to Idle`() = runTest {
        coEvery { sessionStore.getUser() } returns sampleUser
        coEvery { logoutUseCase() } returns Result.success(Unit)
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(sampleUser, vm.currentUser.value)

        vm.logout()
        advanceUntilIdle()

        assertNull(vm.currentUser.value)
        assertEquals(AuthUiState.Idle, vm.uiState.value)
        coVerify { logoutUseCase() }
    }

    // ── resetState ───────────────────────────────────────────────────────────

    @Test
    fun `resetState returns uiState to Idle after Error`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns
        Result.failure(IllegalStateException("err"))
        val vm = viewModel()
        advanceUntilIdle()
        vm.login("a@b.c", "x")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is AuthUiState.Error)

        vm.resetState()

        assertEquals(AuthUiState.Idle, vm.uiState.value)
    }
}