package com.adaptive_tutor_mobile.domain.model

import com.adaptive_tutor_mobile.data.remote.dto.UserDataResponse
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserMapperTest {

    private fun dto(role: String = "STUDENT", orgId: String? = "org-1") = UserDataResponse(
            id = "u1", firstName = "Ana", lastName = "Pop",
            email = "ana@x.com", role = role, status = "ACTIVE",
            organizationId = orgId, organizationName = "School",
            organizationType = "SCHOOL", country = "RO", city = "Iași",
            organizationPhoneNumber = null, organizationAddress = null
    )

    @Test
    fun `maps each known role string to enum`() {
        assertEquals(UserRole.ADMIN,              dto("ADMIN").toDomain().role)
        assertEquals(UserRole.ORGANIZATION_ADMIN, dto("ORGANIZATION_ADMIN").toDomain().role)
        assertEquals(UserRole.TEACHER,            dto("TEACHER").toDomain().role)
        assertEquals(UserRole.STUDENT,            dto("STUDENT").toDomain().role)
        assertEquals(UserRole.PARENT,             dto("PARENT").toDomain().role)
    }

    @Test
    fun `maps unknown role string to UNKNOWN`() {
        assertEquals(UserRole.UNKNOWN, dto("WIZARD").toDomain().role)
    }

    @Test
    fun `maps empty role string to UNKNOWN`() {
        assertEquals(UserRole.UNKNOWN, dto("").toDomain().role)
    }

    @Test
    fun `preserves nullable organization fields`() {
        val user = dto(orgId = null).toDomain()
        assertNull(user.organizationId)
    }

    @Test
    fun `copies basic fields straight through`() {
        val user = dto().toDomain()
        assertEquals("u1", user.id)
        assertEquals("Ana", user.firstName)
        assertEquals("Pop", user.lastName)
        assertEquals("ana@x.com", user.email)
        assertEquals("ACTIVE", user.status)
    }
}