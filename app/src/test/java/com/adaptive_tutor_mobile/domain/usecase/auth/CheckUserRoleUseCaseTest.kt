package com.adaptive_tutor_mobile.domain.usecase.auth

import com.adaptive_tutor_mobile.di.SessionStore
import com.adaptive_tutor_mobile.domain.model.auth.User
import com.adaptive_tutor_mobile.domain.model.auth.UserRole
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class CheckUserRoleUseCaseTest {

    private val sessionStore: SessionStore = mockk()
    private val useCase = CheckUserRoleUseCase(sessionStore)

    @Test
    fun `invoke returns STUDENT when session contains student user`() = runTest {
        coEvery { sessionStore.getUser() } returns sampleUser(UserRole.STUDENT)

        assertEquals(UserRole.STUDENT, useCase())
    }

    @Test
    fun `invoke returns ADMIN when session contains admin user`() = runTest {
        coEvery { sessionStore.getUser() } returns sampleUser(UserRole.ADMIN)

        assertEquals(UserRole.ADMIN, useCase())
    }

    @Test
    fun `invoke returns UNKNOWN when session is empty`() = runTest {
        coEvery { sessionStore.getUser() } returns null

        assertEquals(UserRole.UNKNOWN, useCase())
    }

    private fun sampleUser(role: UserRole) = User(
        id = "user-1",
        firstName = "Ana",
        lastName = "Pop",
        email = "ana@example.com",
        role = role,
        status = "ACTIVE",
        organizationId = null,
        organizationName = null
    )
}
