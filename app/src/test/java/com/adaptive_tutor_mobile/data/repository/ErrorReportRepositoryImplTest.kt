package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.ErrorReportApi
import com.adaptive_tutor_mobile.data.remote.dto.ErrorReportRequestDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class ErrorReportRepositoryImplTest {

    private lateinit var api: ErrorReportApi
    private lateinit var repo: ErrorReportRepositoryImpl

    @Before
    fun setup() {
        api = mockk()
        repo = ErrorReportRepositoryImpl(api)
    }

    @Test
    fun `success returns Result success and passes correct params`() = runTest {
        coEvery { api.reportError(any(), any()) } returns Response.success(Unit)

        val result = repo.reportError(
            questionId = 42,
            description = "Răspunsul corect e marcat greșit"
        )

        assertTrue(result.isSuccess)
        coVerify {
            api.reportError(
                42,
                match<ErrorReportRequestDto> { it.description == "Răspunsul corect e marcat greșit" }
            )
        }
    }

    @Test
    fun `error with json body parses message field`() = runTest {
        val body = """{"timestamp":"2025-01-01","status":400,"message":"Description too short"}"""
            .toResponseBody("application/json".toMediaType())
        coEvery { api.reportError(any(), any()) } returns Response.error(400, body)

        val result = repo.reportError(1, "scurt")

        assertTrue(result.isFailure)
        assertEquals("Description too short", result.exceptionOrNull()?.message)
    }

    @Test
    fun `error 400 without body falls back to default message`() = runTest {
        coEvery { api.reportError(any(), any()) } returns Response.error(
            400, "".toResponseBody("application/json".toMediaType())
        )

        val result = repo.reportError(1, "ceva")

        assertTrue(result.isFailure)
        assertEquals(
            "Descrierea trebuie să aibă între 10 și 2000 de caractere",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun `error 401 without body falls back to default message`() = runTest {
        coEvery { api.reportError(any(), any()) } returns Response.error(
            401, "".toResponseBody("application/json".toMediaType())
        )

        val result = repo.reportError(1, "ceva")

        assertTrue(result.isFailure)
        assertEquals("Sesiune expirată", result.exceptionOrNull()?.message)
    }

    @Test
    fun `error 403 without body falls back to default message`() = runTest {
        coEvery { api.reportError(any(), any()) } returns Response.error(
            403, "".toResponseBody("application/json".toMediaType())
        )

        val result = repo.reportError(1, "ceva")

        assertTrue(result.isFailure)
        assertEquals("Nu ai acces la această întrebare", result.exceptionOrNull()?.message)
    }

    @Test
    fun `error 404 without body falls back to default message`() = runTest {
        coEvery { api.reportError(any(), any()) } returns Response.error(
            404, "".toResponseBody("application/json".toMediaType())
        )

        val result = repo.reportError(999, "ceva")

        assertTrue(result.isFailure)
        assertEquals("Întrebarea nu a fost găsită", result.exceptionOrNull()?.message)
    }

    @Test
    fun `error 500 with malformed body falls back to generic code message`() = runTest {
        coEvery { api.reportError(any(), any()) } returns Response.error(
            500,
            "not a json".toResponseBody("application/json".toMediaType())
        )

        val result = repo.reportError(1, "ceva")

        assertTrue(result.isFailure)
        assertEquals("Eroare 500", result.exceptionOrNull()?.message)
    }
}