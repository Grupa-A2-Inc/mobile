package com.adaptive_tutor_mobile.presentation.profile

import app.cash.turbine.test
import com.adaptive_tutor_mobile.di.SessionStore
import com.adaptive_tutor_mobile.domain.model.auth.User
import com.adaptive_tutor_mobile.domain.model.profile.UserProfile
import com.adaptive_tutor_mobile.domain.model.auth.UserRole
import com.adaptive_tutor_mobile.domain.usecase.profile.ChangePasswordUseCase
import com.adaptive_tutor_mobile.domain.usecase.profile.GetUserProfileUseCase
import com.adaptive_tutor_mobile.domain.usecase.profile.UpdateUserProfileUseCase
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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getProfileUseCase: GetUserProfileUseCase
    private lateinit var updateProfileUseCase: UpdateUserProfileUseCase
    private lateinit var changePasswordUseCase: ChangePasswordUseCase
    private lateinit var sessionStore: SessionStore

    private val sampleUser = User(
        id               = "user-1",
        firstName        = "Ana",
        lastName         = "Pop",
        email            = "ana@example.com",
        role             = UserRole.STUDENT,
        status           = "ACTIVE",
        organizationId   = "org-1",
        organizationName = "School X"
    )

    private val sampleProfile = UserProfile(
        id               = "user-1",
        firstName        = "Ana",
        lastName         = "Pop",
        email            = "ana@example.com",
        organizationName = "School X",
        country          = "RO",
        city             = "Iași"
    )

    @Before
    fun setup() {
        getProfileUseCase    = mockk()
        updateProfileUseCase = mockk()
        changePasswordUseCase = mockk()
        sessionStore = mockk(relaxUnitFun = true) {
            coEvery { getUser() } returns sampleUser
            coEvery { saveUser(any()) } returns Unit
        }
    }

    private fun viewModel() = ProfileViewModel(
        getProfileUseCase,
        updateProfileUseCase,
        changePasswordUseCase,
        sessionStore
    )

    // ── init / loadProfile ────────────────────────────────────────────────────

    @Test
    fun `init loads profile and sets organizationName from API response`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns Result.success(sampleProfile)

        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(sampleProfile, vm.uiState.value.profile)
        assertEquals("School X",    vm.uiState.value.organizationName)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `init falls back to SessionStore organizationName when API returns null`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns
                Result.success(sampleProfile.copy(organizationName = null))

        val vm = viewModel()
        advanceUntilIdle()

        // SessionStore has "School X" — must be kept
        assertEquals("School X", vm.uiState.value.organizationName)
    }

    @Test
    fun `init sets organizationName from SessionStore even when API returns null orgName`() = runTest {
        // API returns profile without organizationName — SessionStore value must survive
        coEvery { getProfileUseCase("user-1") } returns
                Result.success(sampleProfile.copy(organizationName = null))

        val vm = viewModel()
        advanceUntilIdle()

        // SessionStore had "School X"; API returned null → fallback must be kept
        assertEquals("School X", vm.uiState.value.organizationName)
    }

    @Test
    fun `init failure sets errorMessage and clears isLoading`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns
                Result.failure(Exception("Server error"))

        val vm = viewModel()
        advanceUntilIdle()

        assertNull(vm.uiState.value.profile)
        assertFalse(vm.uiState.value.isLoading)
        assertEquals("Server error", vm.uiState.value.errorMessage)
    }

    @Test
    fun `init failure with null message uses default error text`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns
                Result.failure(RuntimeException())

        val vm = viewModel()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.errorMessage!!.isNotBlank())
    }

    @Test
    fun `loadProfile when session is empty does nothing`() = runTest {
        coEvery { sessionStore.getUser() } returns null
        val vm = viewModel()
        advanceUntilIdle()

        assertNull(vm.uiState.value.profile)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `loadProfile sets isLoading true then false on success`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns Result.success(sampleProfile)

        val vm = viewModel()

        vm.uiState.test {
            // initial
            val initial = awaitItem()
            assertFalse(initial.isLoading)

            // isLoading = true (set synchronously in loadProfile before launch)
            val loading = awaitItem()
            assertTrue(loading.isLoading)

            // success
            val success = awaitItem()
            assertFalse(success.isLoading)
            assertEquals(sampleProfile, success.profile)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── updateProfile ─────────────────────────────────────────────────────────

    @Test
    fun `updateProfile success updates state and sets successMessage`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns Result.success(sampleProfile)
        val updated = sampleProfile.copy(firstName = "Maria")
        coEvery {
            updateProfileUseCase("user-1", "ana@example.com", "Maria", "Pop", null)
        } returns Result.success(updated)

        val vm = viewModel()
        advanceUntilIdle()

        vm.updateProfile(email = "ana@example.com", firstName = "Maria", lastName = "Pop")
        advanceUntilIdle()

        assertEquals(updated, vm.uiState.value.profile)
        assertEquals("Profil actualizat cu succes", vm.uiState.value.successMessage)
        assertFalse(vm.uiState.value.isSaving)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `updateProfile preserves organizationName when API returns null`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns Result.success(sampleProfile)
        coEvery {
            updateProfileUseCase(any(), any(), any(), any(), any())
        } returns Result.success(sampleProfile.copy(organizationName = null))

        val vm = viewModel()
        advanceUntilIdle()

        vm.updateProfile("ana@example.com", "Ana", "Pop")
        advanceUntilIdle()

        // Should keep the previously loaded organizationName
        assertEquals("School X", vm.uiState.value.organizationName)
    }

    @Test
    fun `updateProfile saves updated user to SessionStore`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns Result.success(sampleProfile)
        coEvery {
            updateProfileUseCase(any(), any(), any(), any(), any())
        } returns Result.success(sampleProfile.copy(firstName = "Maria"))

        val vm = viewModel()
        advanceUntilIdle()

        vm.updateProfile("ana@example.com", "Maria", "Pop")
        advanceUntilIdle()

        coVerify { sessionStore.saveUser(match { it.firstName == "Maria" }) }
    }

    @Test
    fun `updateProfile failure sets errorMessage and clears isSaving`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns Result.success(sampleProfile)
        coEvery {
            updateProfileUseCase(any(), any(), any(), any(), any())
        } returns Result.failure(Exception("Emailul este deja folosit de un alt cont"))

        val vm = viewModel()
        advanceUntilIdle()

        vm.updateProfile("other@example.com", "Ana", "Pop")
        advanceUntilIdle()

        assertEquals(
            "Emailul este deja folosit de un alt cont",
            vm.uiState.value.errorMessage
        )
        assertFalse(vm.uiState.value.isSaving)
        assertNull(vm.uiState.value.successMessage)
    }

    @Test
    fun `updateProfile failure with null message uses default`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns Result.success(sampleProfile)
        coEvery {
            updateProfileUseCase(any(), any(), any(), any(), any())
        } returns Result.failure(RuntimeException())

        val vm = viewModel()
        advanceUntilIdle()

        vm.updateProfile("a@b.c", "A", "B")
        advanceUntilIdle()

        assertTrue(vm.uiState.value.errorMessage!!.isNotBlank())
    }

    @Test
    fun `updateProfile when session is empty does nothing`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns Result.success(sampleProfile)
        // Return user on first call (init), null on second (updateProfile)
        coEvery { sessionStore.getUser() } returnsMany listOf(sampleUser, null)

        val vm = viewModel()
        advanceUntilIdle()

        vm.updateProfile("a@b.c", "A", "B")
        advanceUntilIdle()

        // isSaving should never have been set (early return)
        assertFalse(vm.uiState.value.isSaving)
        assertNull(vm.uiState.value.successMessage)
    }

    // ── changePassword — client validation ────────────────────────────────────

    @Test
    fun `changePassword with newPassword shorter than 8 chars sets errorMessage immediately`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns Result.success(sampleProfile)
        val vm = viewModel()
        advanceUntilIdle()

        vm.changePassword("oldPass", "short", "short")

        assertEquals(
            "Parola nouă trebuie să aibă minim 8 caractere",
            vm.uiState.value.errorMessage
        )
    }

    @Test
    fun `changePassword with exactly 7 chars sets length error`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns Result.success(sampleProfile)
        val vm = viewModel()
        advanceUntilIdle()

        vm.changePassword("oldPass", "1234567", "1234567")

        assertTrue(vm.uiState.value.errorMessage!!.contains("8"))
    }

    @Test
    fun `changePassword with mismatched passwords sets mismatch error`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns Result.success(sampleProfile)
        val vm = viewModel()
        advanceUntilIdle()

        vm.changePassword("oldPass", "newPass123", "newPass456")

        assertEquals("Parolele nu coincid", vm.uiState.value.errorMessage)
    }

    @Test
    fun `changePassword length error does not call use case`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns Result.success(sampleProfile)
        val vm = viewModel()
        advanceUntilIdle()

        vm.changePassword("oldPass", "short", "short")
        advanceUntilIdle()

        coVerify(exactly = 0) { changePasswordUseCase(any(), any(), any(), any()) }
    }

    @Test
    fun `changePassword mismatch error does not call use case`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns Result.success(sampleProfile)
        val vm = viewModel()
        advanceUntilIdle()

        vm.changePassword("oldPass", "newPass123", "newPass456")
        advanceUntilIdle()

        coVerify(exactly = 0) { changePasswordUseCase(any(), any(), any(), any()) }
    }

    // ── changePassword — API success / failure ────────────────────────────────

    @Test
    fun `changePassword success sets successMessage and clears isSaving`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns Result.success(sampleProfile)
        coEvery {
            changePasswordUseCase("user-1", "oldPass", "newPass123", "newPass123")
        } returns Result.success(Unit)

        val vm = viewModel()
        advanceUntilIdle()

        vm.changePassword("oldPass", "newPass123", "newPass123")
        advanceUntilIdle()

        assertEquals("Parola a fost schimbată cu succes", vm.uiState.value.successMessage)
        assertFalse(vm.uiState.value.isSaving)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `changePassword with exactly 8 chars passes validation and calls use case`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns Result.success(sampleProfile)
        coEvery {
            changePasswordUseCase("user-1", "old", "exactly8", "exactly8")
        } returns Result.success(Unit)

        val vm = viewModel()
        advanceUntilIdle()

        vm.changePassword("old", "exactly8", "exactly8")
        advanceUntilIdle()

        coVerify { changePasswordUseCase("user-1", "old", "exactly8", "exactly8") }
        assertEquals("Parola a fost schimbată cu succes", vm.uiState.value.successMessage)
    }

    @Test
    fun `changePassword failure sets errorMessage and clears isSaving`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns Result.success(sampleProfile)
        coEvery {
            changePasswordUseCase(any(), any(), any(), any())
        } returns Result.failure(Exception("Parola curentă este incorectă"))

        val vm = viewModel()
        advanceUntilIdle()

        vm.changePassword("wrongOld", "newPass123", "newPass123")
        advanceUntilIdle()

        assertEquals("Parola curentă este incorectă", vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isSaving)
        assertNull(vm.uiState.value.successMessage)
    }

    @Test
    fun `changePassword failure with null message uses default`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns Result.success(sampleProfile)
        coEvery {
            changePasswordUseCase(any(), any(), any(), any())
        } returns Result.failure(RuntimeException())

        val vm = viewModel()
        advanceUntilIdle()

        vm.changePassword("oldPass", "newPass123", "newPass123")
        advanceUntilIdle()

        assertTrue(vm.uiState.value.errorMessage!!.isNotBlank())
    }

    @Test
    fun `changePassword when session is empty does nothing after validation`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns Result.success(sampleProfile)
        // loadProfile() apelează getUser() o singură dată;
        // changePassword() apelează getUser() o singură dată după validare
        coEvery { sessionStore.getUser() } returnsMany listOf(sampleUser, null)
        // changePasswordUseCase nu trebuie mockat — nu ar trebui apelat

        val vm = viewModel()
        advanceUntilIdle()

        vm.changePassword("oldPass", "newPass123", "newPass123")
        advanceUntilIdle()

        coVerify(exactly = 0) { changePasswordUseCase(any(), any(), any(), any()) }
    }

    // ── clearMessages ─────────────────────────────────────────────────────────

    @Test
    fun `clearMessages resets successMessage and errorMessage to null`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns
                Result.failure(Exception("Some error"))

        val vm = viewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.errorMessage != null)

        vm.clearMessages()

        assertNull(vm.uiState.value.successMessage)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `clearMessages does not affect profile or other state fields`() = runTest {
        coEvery { getProfileUseCase("user-1") } returns Result.success(sampleProfile)

        val vm = viewModel()
        advanceUntilIdle()
        val profileBefore = vm.uiState.value.profile

        vm.clearMessages()

        assertEquals(profileBefore, vm.uiState.value.profile)
        assertEquals("School X", vm.uiState.value.organizationName)
    }
}