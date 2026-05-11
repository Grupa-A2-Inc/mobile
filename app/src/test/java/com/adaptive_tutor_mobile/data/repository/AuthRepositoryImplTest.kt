package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.AuthApi
import com.adaptive_tutor_mobile.data.remote.dto.AuthResponse
import com.adaptive_tutor_mobile.data.remote.dto.ForgotPasswordRequest
import com.adaptive_tutor_mobile.data.remote.dto.LoginRequest
import com.adaptive_tutor_mobile.data.remote.dto.RegisterRequest
import com.adaptive_tutor_mobile.data.remote.dto.UserDataResponse
import com.adaptive_tutor_mobile.di.SessionStore
import com.adaptive_tutor_mobile.domain.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRepositoryImplTest {

    private lateinit var api: AuthApi
    private lateinit var sessionStore: SessionStore
    private lateinit var repository: AuthRepositoryImpl

    private val sampleUser = UserDataResponse(
            id = "user-123",
            firstName = "Ana",
            lastName = "Pop",
            email = "ana@example.com",
            role = "STUDENT",
            status = "ACTIVE",
            organizationId = "org-1",
            organizationName = "School X",
            organizationType = "SCHOOL",
            country = "RO",
            city = "Iași",
            organizationPhoneNumber = null,
            organizationAddress = null
    )

    @Before
    fun setup() {
        api = mockk()
        sessionStore = mockk(relaxUnitFun = true) {
            coEvery { saveUser(any()) } returns Unit
            coEvery { clearAll() } returns Unit
        }
        repository = AuthRepositoryImpl(api, sessionStore)
    }

    // ── login ────────────────────────────────────────────────────────────────

    @Test
    fun `login success saves token and user and returns user`() = runTest {
        val response = AuthResponse("OK", "access-token-abc", null, sampleUser)
        coEvery { api.login(any()) } returns Response.success(response)

        val result = repository.login("ana@example.com", "password123")

        assertTrue(result.isSuccess)
        val user = result.getOrNull()!!
                assertEquals("user-123", user.id)
        assertEquals("ana@example.com", user.email)
        coVerify { sessionStore.saveAccessToken("access-token-abc") }
        coVerify { sessionStore.saveUser(any<User>()) }
    }

    @Test
    fun `login failure with json body parses message field`() = runTest {
        val body = """{"timestamp":"2025-01-01","status":401,"message":"Bad credentials"}"""
                .toResponseBody("application/json".toMediaType())
        coEvery { api.login(any()) } returns Response.error(401, body)

        val result = repository.login("x@y.z", "wrong")

        assertTrue(result.isFailure)
        assertEquals("Bad credentials", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login 401 without body falls back to default message`() = runTest {
        val body = "".toResponseBody("application/json".toMediaType())
        coEvery { api.login(any()) } returns Response.error(401, body)

        val result = repository.login("x@y.z", "wrong")

        assertTrue(result.isFailure)
        assertEquals("Email sau parolă incorecte", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login passes credentials to api unchanged`() = runTest {
        coEvery { api.login(any()) } returns Response.success(
                AuthResponse("OK", "tok", null, sampleUser)
        )

        repository.login("user@example.com", "secret123")

        coVerify {
            api.login(match<LoginRequest> {
                    it.email == "user@example.com" && it.password == "secret123"
            })
        }
    }

    @Test
    fun `login empty body throws inside Result`() = runTest {
        coEvery { api.login(any()) } returns Response.success(null)

        val result = repository.login("a@b.c", "pwd")

        assertTrue(result.isFailure)
    }

    // ── register ─────────────────────────────────────────────────────────────

    @Test
    fun `register success saves token and user`() = runTest {
        val request = RegisterRequest(
                firstName = "Ion", lastName = "Popescu",
                email = "ion@x.com", password = "password123",
                confirmPassword = "password123",
                organizationName = "Org", country = "RO",
                city = "Iași", organizationType = "SCHOOL"
        )
        coEvery { api.register(request) } returns Response.success(
                AuthResponse("OK", "new-token", null, sampleUser)
        )

        val result = repository.register(request)

        assertTrue(result.isSuccess)
        coVerify { sessionStore.saveAccessToken("new-token") }
        coVerify { sessionStore.saveUser(any<User>()) }
    }

    @Test
    fun `register 409 returns parsed conflict error`() = runTest {
        val body = """{"message":"Email already exists"}"""
                .toResponseBody("application/json".toMediaType())
        coEvery { api.register(any()) } returns Response.error(409, body)

        val result = repository.register(
                RegisterRequest("a", "b", "a@b.c", "12345678", "12345678",
                        "Org", "RO", "Iași", "SCHOOL")
        )

        assertTrue(result.isFailure)
        assertEquals("Email already exists", result.exceptionOrNull()?.message)
    }

    // ── logout ───────────────────────────────────────────────────────────────

    @Test
    fun `logout calls api and clears session`() = runTest {
        coEvery { api.logout() } returns Response.success(Unit)

        val result = repository.logout()

        assertTrue(result.isSuccess)
        coVerify { api.logout() }
        coVerify { sessionStore.clearAll() }
    }

    @Test
    fun `logout clears session even when api throws`() = runTest {
        coEvery { api.logout() } throws RuntimeException("No network")

        val result = repository.logout()

        assertTrue(result.isSuccess)
        coVerify { sessionStore.clearAll() }
    }

    // ── forgot password ──────────────────────────────────────────────────────

    @Test
    fun `forgotPassword success returns Unit and passes email`() = runTest {
        coEvery { api.forgotPassword(any()) } returns Response.success(Unit)

        val result = repository.forgotPassword("user@example.com")

        assertTrue(result.isSuccess)
        coVerify {
            api.forgotPassword(match<ForgotPasswordRequest> { it.email == "user@example.com" })
        }
    }

    @Test
    fun `forgotPassword http failure returns parsed error`() = runTest {
        val body = """{"message":"Server down"}"""
                .toResponseBody("application/json".toMediaType())
        coEvery { api.forgotPassword(any()) } returns Response.error(500, body)

        val result = repository.forgotPassword("user@example.com")

        assertTrue(result.isFailure)
        assertEquals("Server down", result.exceptionOrNull()?.message)
    }
}