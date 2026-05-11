package com.adaptive_tutor_mobile.domain.usecase

import com.adaptive_tutor_mobile.data.remote.dto.RegisterRequest
import com.adaptive_tutor_mobile.domain.model.User
import com.adaptive_tutor_mobile.domain.model.UserRole
import com.adaptive_tutor_mobile.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthUseCasesTest {

    private val repo: AuthRepository = mockk()
    private val user = User(
            "1", "A", "B", "a@b.c", UserRole.STUDENT, "ACTIVE", null, null
    )

    @Test
    fun `LoginUseCase delegates to repository with email and password`() = runTest {
        coEvery { repo.login("a@b.c", "pwd") } returns Result.success(user)

        val result = LoginUseCase(repo).invoke("a@b.c", "pwd")

        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
        coVerify { repo.login("a@b.c", "pwd") }
    }

    @Test
    fun `RegisterUseCase delegates to repository with request`() = runTest {
        val req = RegisterRequest(
                "A", "B", "a@b.c", "12345678", "12345678",
                "Org", "RO", "Iași", "SCHOOL"
        )
        coEvery { repo.register(req) } returns Result.success(user)

        val result = RegisterUseCase(repo).invoke(req)

        assertTrue(result.isSuccess)
        coVerify { repo.register(req) }
    }

    @Test
    fun `LogoutUseCase delegates to repository`() = runTest {
        coEvery { repo.logout() } returns Result.success(Unit)

        val result = LogoutUseCase(repo).invoke()

        assertTrue(result.isSuccess)
        coVerify { repo.logout() }
    }

    @Test
    fun `ForgotPasswordUseCase delegates to repository with email`() = runTest {
        coEvery { repo.forgotPassword("a@b.c") } returns Result.success(Unit)

        val result = ForgotPasswordUseCase(repo).invoke("a@b.c")

        assertTrue(result.isSuccess)
        coVerify { repo.forgotPassword("a@b.c") }
    }
}