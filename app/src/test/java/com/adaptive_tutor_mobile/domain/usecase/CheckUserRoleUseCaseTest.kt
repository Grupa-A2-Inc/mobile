package com.adaptive_tutor_mobile.domain.usecase

import com.adaptive_tutor_mobile.di.SessionStore
import com.adaptive_tutor_mobile.domain.model.auth.User
import com.adaptive_tutor_mobile.domain.model.auth.UserRole
import com.adaptive_tutor_mobile.domain.usecase.auth.CheckUserRoleUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class CheckUserRoleUseCaseTest {

    private val sessionStore: SessionStore = mockk()
    private val useCase = CheckUserRoleUseCase(sessionStore)

    @Test
    fun `returns stored user role when session exists`() = runTest {
        coEvery { sessionStore.getUser() } returns User(
            id = "1",
            firstName = "Ana",
            lastName = "Pop",
            email = "ana@example.com",
            role = UserRole.TEACHER,
            status = "ACTIVE",
            organizationId = null,
            organizationName = null
        )

        assertEquals(UserRole.TEACHER, useCase())
    }

    @Test
    fun `returns unknown when session is empty`() = runTest {
        coEvery { sessionStore.getUser() } returns null

        assertEquals(UserRole.UNKNOWN, useCase())
    }
}
