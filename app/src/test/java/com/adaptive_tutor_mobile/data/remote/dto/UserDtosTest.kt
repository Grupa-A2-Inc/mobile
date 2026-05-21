package com.adaptive_tutor_mobile.data.remote.dto

import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class UserDtosTest {

    @Test
    fun `UserProfileDto stores all fields`() {
        val dto = UserProfileDto(
            id = "u1",
            firstName = "Ada",
            lastName = "Lovelace",
            email = "ada@example.com",
            organizationName = "Analytical Engine",
            country = "UK",
            city = "London"
        )

        assertEquals("u1", dto.id)
        assertEquals("Ada", dto.firstName)
        assertEquals("Lovelace", dto.lastName)
        assertEquals("ada@example.com", dto.email)
        assertEquals("Analytical Engine", dto.organizationName)
        assertEquals("UK", dto.country)
        assertEquals("London", dto.city)
    }

    @Test
    fun `UpdateUserDto copy keeps data class behavior`() {
        val dto = UpdateUserDto(
            email = "alan@example.com",
            firstName = "Alan",
            lastName = "Turing",
            organizationId = null
        )
        val updated = dto.copy(email = "alan2@example.com")

        assertEquals("Alan", updated.firstName)
        assertEquals("Turing", updated.lastName)
        assertEquals("alan2@example.com", updated.email)
        assertNull(dto.organizationId)
    }

    @Test
    fun `ChangePasswordDto stores password fields`() {
        val dto = ChangePasswordDto(
            currentPassword = "old-secret",
            newPassword = "new-secret",
            newPasswordConfirm = "new-secret"
        )

        assertEquals("old-secret", dto.currentPassword)
        assertEquals("new-secret", dto.newPassword)
        assertEquals("new-secret", dto.newPasswordConfirm)
    }
}
