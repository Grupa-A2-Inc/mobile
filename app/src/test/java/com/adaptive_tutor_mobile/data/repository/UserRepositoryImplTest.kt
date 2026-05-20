package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.UserApi
import com.adaptive_tutor_mobile.data.remote.dto.UserProfileDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody

class UserRepositoryImplTest {

    private lateinit var api: UserApi
    private lateinit var repository: UserRepositoryImpl

    private val userId = "user-123"
    private val profileDto = UserProfileDto(
        id = userId,
        firstName = "Ada",
        lastName = "Lovelace",
        email = "ada@example.com",
        organizationName = "OpenAI",
        country = "UK",
        city = "London"
    )

    @Before
    fun setUp() {
        api = mockk()
        repository = UserRepositoryImpl(api)
    }

    @Test
    fun `getProfile maps successful response`() = runTest {
        coEvery { api.getUserById(userId) } returns Response.success(profileDto)

        val result = repository.getProfile(userId)

        assertTrue(result.isSuccess)
        assertEquals(profileDto.id, result.getOrNull()?.id)
        assertEquals(profileDto.firstName, result.getOrNull()?.firstName)
        assertEquals(profileDto.city, result.getOrNull()?.city)
    }

    @Test
    fun `getProfile returns failure on error response`() = runTest {
        coEvery { api.getUserById(userId) } returns Response.error(404, okhttp3.ResponseBody.create(null, ""))

        val result = repository.getProfile(userId)

        assertTrue(result.isFailure)
    }

    @Test
    fun `getProfile returns failure on exception`() = runTest {
        coEvery { api.getUserById(userId) } throws RuntimeException("boom")

        val result = repository.getProfile(userId)

        assertTrue(result.isFailure)
        assertEquals("boom", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateProfile maps successful response`() = runTest {
        coEvery { api.updateUser(userId, any()) } returns Response.success(profileDto.copy(city = "Bucharest"))

        val result = repository.updateProfile(userId, "ada@example.com", "Ada", "Lovelace", null)

        assertTrue(result.isSuccess)
        assertEquals("Bucharest", result.getOrNull()?.city)
    }

    @Test
    fun `updateProfile returns failure on error response`() = runTest {
        coEvery { api.updateUser(userId, any()) } returns Response.error(400, okhttp3.ResponseBody.create(null, ""))

        val result = repository.updateProfile(userId, "ada@example.com", "Ada", "Lovelace", null)

        assertTrue(result.isFailure)
    }

    @Test
    fun `updateProfile returns failure on exception`() = runTest {
        coEvery { api.updateUser(userId, any()) } throws IllegalStateException("update failed")

        val result = repository.updateProfile(userId, "ada@example.com", "Ada", "Lovelace", null)

        assertTrue(result.isFailure)
        assertEquals("update failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `changePassword returns success on successful response`() = runTest {
        coEvery { api.changePassword(userId, any()) } returns Response.success(Unit)

        val result = repository.changePassword(userId, "old", "new", "new")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `changePassword returns failure on error response`() = runTest {
        coEvery { api.changePassword(userId, any()) } returns Response.error(403, okhttp3.ResponseBody.create(null, ""))

        val result = repository.changePassword(userId, "old", "new", "new")

        assertTrue(result.isFailure)
    }

    @Test
    fun `changePassword returns failure on exception`() = runTest {
        coEvery { api.changePassword(userId, any()) } throws RuntimeException("password failed")

        val result = repository.changePassword(userId, "old", "new", "new")

        assertTrue(result.isFailure)
        assertEquals("password failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProfile returns failure with parsed error message from json`() = runTest {
        val body = """{"message":"utilizator negăsit"}"""
            .toResponseBody("application/json".toMediaType())
        coEvery { api.getUserById(userId) } returns Response.error(404, body)

        val result = repository.getProfile(userId)

        assertTrue(result.isFailure)
        assertEquals("utilizator negăsit", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProfile returns failure with error field from json`() = runTest {
        val body = """{"error":"not found"}"""
            .toResponseBody("application/json".toMediaType())
        coEvery { api.getUserById(userId) } returns Response.error(404, body)

        val result = repository.getProfile(userId)

        assertTrue(result.isFailure)
        assertEquals("not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProfile returns failure on 401`() = runTest {
        coEvery { api.getUserById(userId) } returns Response.error(401, "".toResponseBody("application/json".toMediaType()))

        val result = repository.getProfile(userId)

        assertTrue(result.isFailure)
        assertEquals("Sesiune expirată, te rugăm să te autentifici din nou", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProfile returns failure on 403`() = runTest {
        coEvery { api.getUserById(userId) } returns Response.error(403, "".toResponseBody("application/json".toMediaType()))

        val result = repository.getProfile(userId)

        assertTrue(result.isFailure)
        assertEquals("Acces interzis", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProfile returns failure on unknown code`() = runTest {
        coEvery { api.getUserById(userId) } returns Response.error(500, "".toResponseBody("application/json".toMediaType()))

        val result = repository.getProfile(userId)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("500") == true)
    }

    @Test
    fun `getProfile returns failure when body is null`() = runTest {
        coEvery { api.getUserById(userId) } returns Response.success(null)

        val result = repository.getProfile(userId)

        assertTrue(result.isFailure)
        assertEquals("Răspuns gol de la server", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateProfile returns fallback profile when body is null`() = runTest {
        coEvery { api.updateUser(userId, any()) } returns Response.success(null)

        val result = repository.updateProfile(userId, "ada@example.com", "Ada", "Lovelace", null)

        assertTrue(result.isSuccess)
        assertEquals(userId, result.getOrNull()?.id)
        assertEquals("Ada", result.getOrNull()?.firstName)
    }

    @Test
    fun `updateProfile returns failure on 401`() = runTest {
        coEvery { api.updateUser(userId, any()) } returns Response.error(401, "".toResponseBody("application/json".toMediaType()))

        val result = repository.updateProfile(userId, "ada@example.com", "Ada", "Lovelace", null)

        assertTrue(result.isFailure)
        assertEquals("Sesiune expirată, te rugăm să te autentifici din nou", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateProfile returns failure on 403`() = runTest {
        coEvery { api.updateUser(userId, any()) } returns Response.error(403, "".toResponseBody("application/json".toMediaType()))

        val result = repository.updateProfile(userId, "ada@example.com", "Ada", "Lovelace", null)

        assertTrue(result.isFailure)
        assertEquals("Acces interzis", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateProfile returns failure on 404`() = runTest {
        coEvery { api.updateUser(userId, any()) } returns Response.error(404, "".toResponseBody("application/json".toMediaType()))

        val result = repository.updateProfile(userId, "ada@example.com", "Ada", "Lovelace", null)

        assertTrue(result.isFailure)
        assertEquals("Utilizatorul nu a fost găsit", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateProfile returns failure on 409`() = runTest {
        coEvery { api.updateUser(userId, any()) } returns Response.error(409, "".toResponseBody("application/json".toMediaType()))

        val result = repository.updateProfile(userId, "ada@example.com", "Ada", "Lovelace", null)

        assertTrue(result.isFailure)
        assertEquals("Emailul este deja folosit de un alt cont", result.exceptionOrNull()?.message)
    }

    @Test
    fun `changePassword returns failure on 400`() = runTest {
        coEvery { api.changePassword(userId, any()) } returns Response.error(400, "".toResponseBody("application/json".toMediaType()))

        val result = repository.changePassword(userId, "old", "new", "new")

        assertTrue(result.isFailure)
        assertEquals("Parola curentă este incorectă", result.exceptionOrNull()?.message)
    }

    @Test
    fun `changePassword returns failure on 401`() = runTest {
        coEvery { api.changePassword(userId, any()) } returns Response.error(401, "".toResponseBody("application/json".toMediaType()))

        val result = repository.changePassword(userId, "old", "new", "new")

        assertTrue(result.isFailure)
        assertEquals("Sesiune expirată, te rugăm să te autentifici din nou", result.exceptionOrNull()?.message)
    }

    @Test
    fun `changePassword returns failure on 404`() = runTest {
        coEvery { api.changePassword(userId, any()) } returns Response.error(404, "".toResponseBody("application/json".toMediaType()))

        val result = repository.changePassword(userId, "old", "new", "new")

        assertTrue(result.isFailure)
        assertEquals("Utilizatorul nu a fost găsit", result.exceptionOrNull()?.message)
    }

    @Test
    fun `changePassword returns failure on 422`() = runTest {
        coEvery { api.changePassword(userId, any()) } returns Response.error(422, "".toResponseBody("application/json".toMediaType()))

        val result = repository.changePassword(userId, "old", "new", "new")

        assertTrue(result.isFailure)
        assertEquals("Parola nouă nu respectă cerințele de securitate", result.exceptionOrNull()?.message)
    }

    @Test
    fun `changePassword returns failure on unknown code`() = runTest {
        coEvery { api.changePassword(userId, any()) } returns Response.error(500, "".toResponseBody("application/json".toMediaType()))

        val result = repository.changePassword(userId, "old", "new", "new")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("500") == true)
    }
}
