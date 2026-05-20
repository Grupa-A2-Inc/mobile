package com.adaptive_tutor_mobile.domain.model

import com.adaptive_tutor_mobile.domain.model.profile.UserProfile
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class UserProfileTest {

    @Test
    fun `UserProfile stores all fields`() {
        val profile = UserProfile(
            id = "user-1",
            firstName = "Ada",
            lastName = "Lovelace",
            email = "ada@example.com",
            organizationName = "OpenAI",
            country = "UK",
            city = "London"
        )

        assertEquals("user-1", profile.id)
        assertEquals("Ada", profile.firstName)
        assertEquals("Lovelace", profile.lastName)
        assertEquals("ada@example.com", profile.email)
        assertEquals("OpenAI", profile.organizationName)
        assertEquals("UK", profile.country)
        assertEquals("London", profile.city)
    }

    @Test
    fun `UserProfile copy updates nullable fields`() {
        val profile = UserProfile(
            id = "user-2",
            firstName = "Alan",
            lastName = "Turing",
            email = "alan@example.com",
            organizationName = null,
            country = null,
            city = null
        )

        val updated = profile.copy(city = "Manchester")

        assertNull(profile.city)
        assertEquals("Manchester", updated.city)
    }
}
