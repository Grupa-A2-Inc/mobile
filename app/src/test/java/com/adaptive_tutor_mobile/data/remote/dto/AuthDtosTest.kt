package com.adaptive_tutor_mobile.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthDtosTest {

    @Test
    fun `LoginRequest holds email and password`() {
        val req = LoginRequest(email = "test@example.com", password = "secret")
        assertEquals("test@example.com", req.email)
        assertEquals("secret", req.password)
    }

    @Test
    fun `RegisterRequest stores all required fields and optional nulls`() {
        val req = RegisterRequest(
            firstName = "Ion",
            lastName = "Popescu",
            email = "ion@example.com",
            password = "pass123",
            confirmPassword = "pass123",
            organizationName = "UBB",
            country = "Romania",
            city = "Cluj",
            organizationType = "UNIVERSITY"
        )
        assertEquals("Ion", req.firstName)
        assertEquals("UNIVERSITY", req.organizationType)
        assertNull(req.address)
        assertNull(req.phoneNumber)
    }

    @Test
    fun `RegisterRequest stores optional fields when provided`() {
        val req = RegisterRequest(
            firstName = "Ana",
            lastName = "Ionescu",
            email = "ana@example.com",
            password = "pass",
            confirmPassword = "pass",
            organizationName = "School",
            country = "Romania",
            city = "Iasi",
            organizationType = "SCHOOL",
            address = "Str. Libertatii 1",
            phoneNumber = "0700000000"
        )
        assertEquals("Str. Libertatii 1", req.address)
        assertEquals("0700000000", req.phoneNumber)
    }

    @Test
    fun `ForgotPasswordRequest holds email`() {
        val req = ForgotPasswordRequest(email = "forgot@example.com")
        assertEquals("forgot@example.com", req.email)
    }

    @Test
    fun `AuthResponse stores tokens and user`() {
        val user = UserDataResponse(
            id = "u1",
            firstName = "Maria",
            lastName = "Pop",
            email = "maria@example.com",
            role = "STUDENT",
            status = "ACTIVE",
            organizationId = "org1",
            organizationName = "TestOrg",
            organizationType = "SCHOOL",
            country = "Romania",
            city = "Timisoara",
            organizationPhoneNumber = null,
            organizationAddress = null
        )
        val response = AuthResponse(
            message = "Login successful",
            accessToken = "access-token",
            refreshToken = "refresh-token",
            user = user
        )
        assertEquals("Login successful", response.message)
        assertEquals("access-token", response.accessToken)
        assertEquals("STUDENT", response.user?.role)
    }

    @Test
    fun `AuthResponse with null fields`() {
        val response = AuthResponse(message = null, accessToken = null, refreshToken = null, user = null)
        assertNull(response.accessToken)
        assertNull(response.user)
    }

    @Test
    fun `UserDataResponse optional fields can be null`() {
        val user = UserDataResponse(
            id = "u2",
            firstName = "X",
            lastName = "Y",
            email = "x@y.com",
            role = "TEACHER",
            status = "PENDING",
            organizationId = null,
            organizationName = null,
            organizationType = null,
            country = null,
            city = null,
            organizationPhoneNumber = null,
            organizationAddress = null
        )
        assertNull(user.organizationId)
        assertNull(user.city)
        assertEquals("TEACHER", user.role)
    }

    @Test
    fun `RefreshResponse holds access token`() {
        val resp = RefreshResponse(accessToken = "new-token")
        assertEquals("new-token", resp.accessToken)
    }

    @Test
    fun `RefreshResponse null token`() {
        val resp = RefreshResponse(accessToken = null)
        assertNull(resp.accessToken)
    }

    @Test
    fun `CsrfResponse holds csrf token and header name`() {
        val resp = CsrfResponse(csrfToken = "csrf-abc", headerName = "X-CSRF-TOKEN")
        assertEquals("csrf-abc", resp.csrfToken)
        assertEquals("X-CSRF-TOKEN", resp.headerName)
    }

    @Test
    fun `CsrfResponse null fields`() {
        val resp = CsrfResponse(csrfToken = null, headerName = null)
        assertNull(resp.csrfToken)
        assertNull(resp.headerName)
    }

    @Test
    fun `data class equality and copy work correctly`() {
        val a = LoginRequest("a@b.com", "pass")
        val b = a.copy()
        assertEquals(a, b)
        val c = a.copy(email = "other@b.com")
        assertEquals("other@b.com", c.email)
        assertEquals("pass", c.password)
    }
}
