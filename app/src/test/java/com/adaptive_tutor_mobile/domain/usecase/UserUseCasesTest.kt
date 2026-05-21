package com.adaptive_tutor_mobile.domain.usecase

import com.adaptive_tutor_mobile.domain.model.profile.UserProfile
import com.adaptive_tutor_mobile.domain.repository.profile.UserRepository
import com.adaptive_tutor_mobile.domain.usecase.profile.ChangePasswordUseCase
import com.adaptive_tutor_mobile.domain.usecase.profile.GetUserProfileUseCase
import com.adaptive_tutor_mobile.domain.usecase.profile.UpdateUserProfileUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserUseCasesTest {

    private lateinit var repository: UserRepository

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
        repository = mockk()
    }

    // ── GetUserProfileUseCase ─────────────────────────────────────────────────

    @Test
    fun `GetUserProfileUseCase success returns profile from repository`() = runTest {
        coEvery { repository.getProfile("user-1") } returns Result.success(sampleProfile)
        val useCase = GetUserProfileUseCase(repository)

        val result = useCase("user-1")

        assertTrue(result.isSuccess)
        assertEquals(sampleProfile, result.getOrNull())
    }

    @Test
    fun `GetUserProfileUseCase failure propagates repository error`() = runTest {
        coEvery { repository.getProfile(any()) } returns
                Result.failure(Exception("Not found"))
        val useCase = GetUserProfileUseCase(repository)

        val result = useCase("user-1")

        assertTrue(result.isFailure)
        assertEquals("Not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `GetUserProfileUseCase delegates userId to repository unchanged`() = runTest {
        coEvery { repository.getProfile("xyz-99") } returns Result.success(sampleProfile)
        val useCase = GetUserProfileUseCase(repository)

        useCase("xyz-99")

        coVerify { repository.getProfile("xyz-99") }
    }

    // ── UpdateUserProfileUseCase ──────────────────────────────────────────────

    @Test
    fun `UpdateUserProfileUseCase success returns updated profile`() = runTest {
        val updated = sampleProfile.copy(firstName = "Maria")
        coEvery {
            repository.updateProfile("user-1", "ana@example.com", "Maria", "Pop", null)
        } returns Result.success(updated)
        val useCase = UpdateUserProfileUseCase(repository)

        val result = useCase("user-1", "ana@example.com", "Maria", "Pop", null)

        assertTrue(result.isSuccess)
        assertEquals("Maria", result.getOrNull()?.firstName)
    }

    @Test
    fun `UpdateUserProfileUseCase failure propagates repository error`() = runTest {
        coEvery {
            repository.updateProfile(any(), any(), any(), any(), any())
        } returns Result.failure(Exception("Conflict"))
        val useCase = UpdateUserProfileUseCase(repository)

        val result = useCase("user-1", "a@b.c", "A", "B", null)

        assertTrue(result.isFailure)
        assertEquals("Conflict", result.exceptionOrNull()?.message)
    }

    @Test
    fun `UpdateUserProfileUseCase passes all parameters to repository unchanged`() = runTest {
        coEvery {
            repository.updateProfile("user-1", "a@b.c", "Ana", "Pop", "org-5")
        } returns Result.success(sampleProfile)
        val useCase = UpdateUserProfileUseCase(repository)

        useCase("user-1", "a@b.c", "Ana", "Pop", "org-5")

        coVerify {
            repository.updateProfile("user-1", "a@b.c", "Ana", "Pop", "org-5")
        }
    }

    @Test
    fun `UpdateUserProfileUseCase passes null organizationId to repository`() = runTest {
        coEvery {
            repository.updateProfile("user-1", "a@b.c", "Ana", "Pop", null)
        } returns Result.success(sampleProfile)
        val useCase = UpdateUserProfileUseCase(repository)

        useCase("user-1", "a@b.c", "Ana", "Pop", null)

        coVerify { repository.updateProfile("user-1", "a@b.c", "Ana", "Pop", null) }
    }

    // ── ChangePasswordUseCase ─────────────────────────────────────────────────

    @Test
    fun `ChangePasswordUseCase success returns Unit`() = runTest {
        coEvery {
            repository.changePassword("user-1", "old", "new12345", "new12345")
        } returns Result.success(Unit)
        val useCase = ChangePasswordUseCase(repository)

        val result = useCase("user-1", "old", "new12345", "new12345")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `ChangePasswordUseCase failure propagates repository error`() = runTest {
        coEvery {
            repository.changePassword(any(), any(), any(), any())
        } returns Result.failure(Exception("Parola curentă este incorectă"))
        val useCase = ChangePasswordUseCase(repository)

        val result = useCase("user-1", "wrong", "new12345", "new12345")

        assertTrue(result.isFailure)
        assertEquals("Parola curentă este incorectă", result.exceptionOrNull()?.message)
    }

    @Test
    fun `ChangePasswordUseCase passes all parameters to repository unchanged`() = runTest {
        coEvery {
            repository.changePassword("user-1", "currentPwd", "newPwd12", "newPwd12")
        } returns Result.success(Unit)
        val useCase = ChangePasswordUseCase(repository)

        useCase("user-1", "currentPwd", "newPwd12", "newPwd12")

        coVerify {
            repository.changePassword("user-1", "currentPwd", "newPwd12", "newPwd12")
        }
    }
}