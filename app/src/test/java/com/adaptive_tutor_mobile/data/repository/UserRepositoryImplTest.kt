package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.UserApi
import com.adaptive_tutor_mobile.data.remote.dto.ChangePasswordDto
import com.adaptive_tutor_mobile.data.remote.dto.UpdateUserDto
import com.adaptive_tutor_mobile.data.remote.dto.UserProfileDto
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

class UserRepositoryImplTest {

    private lateinit var api: UserApi
    private lateinit var repository: UserRepositoryImpl

    private val sampleDto = UserProfileDto(
        id               = "user-1",
        firstName        = "Ana",
        lastName         = "Pop",
        email            = "ana@example.com",
        organizationName = "School X",
        country          = "RO",
        city             = "Iași"
    )

    @Before
    fun setup() {
        api        = mockk()
        repository = UserRepositoryImpl(api)
    }

    // ── getProfile ────────────────────────────────────────────────────────────

    @Test
    fun `getProfile success maps dto to UserProfile`() = runTest {
        coEvery { api.getUserById("user-1") } returns Response.success(sampleDto)

        val result = repository.getProfile("user-1")

        assertTrue(result.isSuccess)
        val profile = result.getOrNull()!!
        assertEquals("user-1",        profile.id)
        assertEquals("Ana",           profile.firstName)
        assertEquals("Pop",           profile.lastName)
        assertEquals("ana@example.com", profile.email)
        assertEquals("School X",      profile.organizationName)
        assertEquals("RO",            profile.country)
        assertEquals("Iași",          profile.city)
    }

    @Test
    fun `getProfile maps null optional fields correctly`() = runTest {
        coEvery { api.getUserById("user-1") } returns Response.success(
            sampleDto.copy(organizationName = null, country = null, city = null)
        )

        val result = repository.getProfile("user-1")

        assertTrue(result.isSuccess)
        val profile = result.getOrNull()!!
        assertEquals(null, profile.organizationName)
        assertEquals(null, profile.country)
        assertEquals(null, profile.city)
    }

    @Test
    fun `getProfile null body returns failure`() = runTest {
        coEvery { api.getUserById(any()) } returns Response.success(null)

        val result = repository.getProfile("user-1")

        assertTrue(result.isFailure)
        assertEquals("Răspuns gol de la server", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProfile 401 returns session expired message`() = runTest {
        coEvery { api.getUserById(any()) } returns Response.error(
            401, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.getProfile("user-1")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("autentifici"))
    }

    @Test
    fun `getProfile 403 returns access denied message`() = runTest {
        coEvery { api.getUserById(any()) } returns Response.error(
            403, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.getProfile("user-1")

        assertTrue(result.isFailure)
        assertEquals("Acces interzis", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProfile 404 returns user not found message`() = runTest {
        coEvery { api.getUserById(any()) } returns Response.error(
            404, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.getProfile("user-1")

        assertTrue(result.isFailure)
        assertEquals("Utilizatorul nu a fost găsit", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProfile unknown code returns generic message with code`() = runTest {
        coEvery { api.getUserById(any()) } returns Response.error(
            500, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.getProfile("user-1")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("500"))
    }

    @Test
    fun `getProfile json body message field takes priority over fallback`() = runTest {
        val body = """{"message":"Token expired"}"""
            .toResponseBody("application/json".toMediaType())
        coEvery { api.getUserById(any()) } returns Response.error(401, body)

        val result = repository.getProfile("user-1")

        assertTrue(result.isFailure)
        assertEquals("Token expired", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProfile json body error field used when message absent`() = runTest {
        val body = """{"error":"Forbidden"}"""
            .toResponseBody("application/json".toMediaType())
        coEvery { api.getUserById(any()) } returns Response.error(403, body)

        val result = repository.getProfile("user-1")

        assertTrue(result.isFailure)
        assertEquals("Forbidden", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProfile malformed json body falls back to http fallback`() = runTest {
        coEvery { api.getUserById(any()) } returns Response.error(
            404, "not-json".toResponseBody("application/json".toMediaType())
        )

        val result = repository.getProfile("user-1")

        assertTrue(result.isFailure)
        assertEquals("Utilizatorul nu a fost găsit", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProfile network exception returns failure with original message`() = runTest {
        coEvery { api.getUserById(any()) } throws RuntimeException("No network")

        val result = repository.getProfile("user-1")

        assertTrue(result.isFailure)
        assertEquals("No network", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProfile passes userId to api unchanged`() = runTest {
        coEvery { api.getUserById("abc-123") } returns Response.success(sampleDto)

        repository.getProfile("abc-123")

        coVerify { api.getUserById("abc-123") }
    }

    // ── updateProfile ─────────────────────────────────────────────────────────

    @Test
    fun `updateProfile success maps returned dto`() = runTest {
        coEvery { api.updateUser("user-1", any()) } returns Response.success(
            sampleDto.copy(firstName = "Maria")
        )

        val result = repository.updateProfile("user-1", "ana@example.com", "Maria", "Pop", null)

        assertTrue(result.isSuccess)
        assertEquals("Maria", result.getOrNull()?.firstName)
    }

    @Test
    fun `updateProfile null body returns fallback UserProfile with sent values`() = runTest {
        coEvery { api.updateUser(any(), any()) } returns Response.success(null)

        val result = repository.updateProfile("user-1", "ana@example.com", "Ana", "Pop", null)

        assertTrue(result.isSuccess)
        val profile = result.getOrNull()!!
        assertEquals("user-1",          profile.id)
        assertEquals("Ana",             profile.firstName)
        assertEquals("Pop",             profile.lastName)
        assertEquals("ana@example.com", profile.email)
        assertEquals(null,              profile.organizationName)
        assertEquals(null,              profile.country)
        assertEquals(null,              profile.city)
    }

    @Test
    fun `updateProfile passes correct dto to api`() = runTest {
        coEvery { api.updateUser("user-1", any()) } returns Response.success(sampleDto)

        repository.updateProfile("user-1", "ana@example.com", "Ana", "Pop", "org-42")

        coVerify {
            api.updateUser(
                "user-1",
                match<UpdateUserDto> {
                    it.email          == "ana@example.com" &&
                            it.firstName      == "Ana"             &&
                            it.lastName       == "Pop"             &&
                            it.organizationId == "org-42"
                }
            )
        }
    }

    @Test
    fun `updateProfile 400 uses default message when body blank`() = runTest {
        coEvery { api.updateUser(any(), any()) } returns Response.error(
            400, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.updateProfile("user-1", "a@b.c", "A", "B", null)

        assertTrue(result.isFailure)
        assertEquals("Date invalide", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateProfile 401 returns session expired message`() = runTest {
        coEvery { api.updateUser(any(), any()) } returns Response.error(
            401, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.updateProfile("user-1", "a@b.c", "A", "B", null)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("autentifici"))
    }

    @Test
    fun `updateProfile 403 returns access denied message`() = runTest {
        coEvery { api.updateUser(any(), any()) } returns Response.error(
            403, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.updateProfile("user-1", "a@b.c", "A", "B", null)

        assertTrue(result.isFailure)
        assertEquals("Acces interzis", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateProfile 404 returns user not found message`() = runTest {
        coEvery { api.updateUser(any(), any()) } returns Response.error(
            404, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.updateProfile("user-1", "a@b.c", "A", "B", null)

        assertTrue(result.isFailure)
        assertEquals("Utilizatorul nu a fost găsit", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateProfile 409 returns email conflict message`() = runTest {
        coEvery { api.updateUser(any(), any()) } returns Response.error(
            409, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.updateProfile("user-1", "a@b.c", "A", "B", null)

        assertTrue(result.isFailure)
        assertEquals("Emailul este deja folosit de un alt cont", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateProfile unknown code returns generic message with code`() = runTest {
        coEvery { api.updateUser(any(), any()) } returns Response.error(
            503, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.updateProfile("user-1", "a@b.c", "A", "B", null)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("503"))
    }

    @Test
    fun `updateProfile json body message field takes priority over fallback`() = runTest {
        val body = """{"message":"Validation failed"}"""
            .toResponseBody("application/json".toMediaType())
        coEvery { api.updateUser(any(), any()) } returns Response.error(400, body)

        val result = repository.updateProfile("user-1", "a@b.c", "A", "B", null)

        assertTrue(result.isFailure)
        assertEquals("Validation failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateProfile json body error field used when message absent`() = runTest {
        val body = """{"error":"Conflict"}"""
            .toResponseBody("application/json".toMediaType())
        coEvery { api.updateUser(any(), any()) } returns Response.error(409, body)

        val result = repository.updateProfile("user-1", "a@b.c", "A", "B", null)

        assertTrue(result.isFailure)
        assertEquals("Conflict", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateProfile malformed json body falls back to http fallback`() = runTest {
        coEvery { api.updateUser(any(), any()) } returns Response.error(
            400, "not-json".toResponseBody("application/json".toMediaType())
        )

        val result = repository.updateProfile("user-1", "a@b.c", "A", "B", null)

        assertTrue(result.isFailure)
        assertEquals("Date invalide", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateProfile network exception returns failure with original message`() = runTest {
        coEvery { api.updateUser(any(), any()) } throws RuntimeException("Timeout")

        val result = repository.updateProfile("user-1", "a@b.c", "A", "B", null)

        assertTrue(result.isFailure)
        assertEquals("Timeout", result.exceptionOrNull()?.message)
    }

    // ── changePassword ────────────────────────────────────────────────────────

    @Test
    fun `changePassword success returns Unit`() = runTest {
        coEvery { api.changePassword("user-1", any()) } returns Response.success(Unit)

        val result = repository.changePassword("user-1", "old123", "new12345", "new12345")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `changePassword passes correct dto to api`() = runTest {
        coEvery { api.changePassword("user-1", any()) } returns Response.success(Unit)

        repository.changePassword("user-1", "oldPass", "newPass8", "newPass8")

        coVerify {
            api.changePassword(
                "user-1",
                match<ChangePasswordDto> {
                    it.currentPassword    == "oldPass"  &&
                            it.newPassword        == "newPass8" &&
                            it.newPasswordConfirm == "newPass8"
                }
            )
        }
    }

    @Test
    fun `changePassword 400 without body returns wrong password message`() = runTest {
        coEvery { api.changePassword(any(), any()) } returns Response.error(
            400, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.changePassword("user-1", "wrong", "new12345", "new12345")

        assertTrue(result.isFailure)
        assertEquals("Parola curentă este incorectă", result.exceptionOrNull()?.message)
    }

    @Test
    fun `changePassword 400 with json message returns server message`() = runTest {
        val body = """{"message":"Current password is incorrect"}"""
            .toResponseBody("application/json".toMediaType())
        coEvery { api.changePassword(any(), any()) } returns Response.error(400, body)

        val result = repository.changePassword("user-1", "wrong", "new12345", "new12345")

        assertTrue(result.isFailure)
        assertEquals("Current password is incorrect", result.exceptionOrNull()?.message)
    }

    @Test
    fun `changePassword 400 with json error field used when message absent`() = runTest {
        val body = """{"error":"Bad request"}"""
            .toResponseBody("application/json".toMediaType())
        coEvery { api.changePassword(any(), any()) } returns Response.error(400, body)

        val result = repository.changePassword("user-1", "wrong", "new12345", "new12345")

        assertTrue(result.isFailure)
        assertEquals("Bad request", result.exceptionOrNull()?.message)
    }

    @Test
    fun `changePassword 400 malformed json falls back to default`() = runTest {
        coEvery { api.changePassword(any(), any()) } returns Response.error(
            400, "not-json".toResponseBody("application/json".toMediaType())
        )

        val result = repository.changePassword("user-1", "wrong", "new12345", "new12345")

        assertTrue(result.isFailure)
        assertEquals("Parola curentă este incorectă", result.exceptionOrNull()?.message)
    }

    @Test
    fun `changePassword 401 returns session expired message`() = runTest {
        coEvery { api.changePassword(any(), any()) } returns Response.error(
            401, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.changePassword("user-1", "old", "new12345", "new12345")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("autentifici"))
    }

    @Test
    fun `changePassword 403 returns access denied message`() = runTest {
        coEvery { api.changePassword(any(), any()) } returns Response.error(
            403, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.changePassword("user-1", "old", "new12345", "new12345")

        assertTrue(result.isFailure)
        assertEquals("Acces interzis", result.exceptionOrNull()?.message)
    }

    @Test
    fun `changePassword 404 returns user not found message`() = runTest {
        coEvery { api.changePassword(any(), any()) } returns Response.error(
            404, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.changePassword("user-1", "old", "new12345", "new12345")

        assertTrue(result.isFailure)
        assertEquals("Utilizatorul nu a fost găsit", result.exceptionOrNull()?.message)
    }

    @Test
    fun `changePassword 422 returns password policy message`() = runTest {
        coEvery { api.changePassword(any(), any()) } returns Response.error(
            422, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.changePassword("user-1", "old", "new12345", "new12345")

        assertTrue(result.isFailure)
        assertEquals("Parola nouă nu respectă cerințele de securitate", result.exceptionOrNull()?.message)
    }

    @Test
    fun `changePassword unknown code returns generic message with code`() = runTest {
        coEvery { api.changePassword(any(), any()) } returns Response.error(
            503, "".toResponseBody("application/json".toMediaType())
        )

        val result = repository.changePassword("user-1", "old", "new12345", "new12345")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("503"))
    }

    @Test
    fun `changePassword network exception returns failure with original message`() = runTest {
        coEvery { api.changePassword(any(), any()) } throws RuntimeException("No internet")

        val result = repository.changePassword("user-1", "old", "new12345", "new12345")

        assertTrue(result.isFailure)
        assertEquals("No internet", result.exceptionOrNull()?.message)
    }
}