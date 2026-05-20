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
}
