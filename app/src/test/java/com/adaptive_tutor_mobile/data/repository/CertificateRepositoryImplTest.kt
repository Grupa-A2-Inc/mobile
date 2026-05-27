package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.CertificateApi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaType
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CertificateRepositoryImplTest {

    private lateinit var api: CertificateApi
    private lateinit var repository: CertificateRepositoryImpl

    private val enrollmentId = "enr-abc-123"
    private val pdfBytes = byteArrayOf(0x25, 0x50, 0x44, 0x46) // PDF magic bytes: %PDF

    @Before
    fun setUp() {
        api = mockk()
        repository = CertificateRepositoryImpl(api)
    }

    // ── 200 OK ───────────────────────────────────────────────────────────────

    @Test
    fun `downloadCertificate returns pdf bytes on 200`() = runTest {
        val body = pdfBytes.toResponseBody("application/pdf".toMediaType())
        coEvery { api.getCertificate(enrollmentId) } returns Response.success(body)

        val result = repository.downloadCertificate(enrollmentId)

        assertTrue(result.isSuccess)
        assertContentEquals(pdfBytes, result.getOrNull())
    }

    @Test
    fun `downloadCertificate returns failure when 200 body is null`() = runTest {
        @Suppress("UNCHECKED_CAST")
        coEvery { api.getCertificate(enrollmentId) } returns Response.success(null) as Response<ResponseBody>

        val result = repository.downloadCertificate(enrollmentId)

        assertFalse(result.isSuccess)
        assertEquals("Răspuns gol de la server", result.exceptionOrNull()?.message)
    }

    // ── 403 Forbidden ────────────────────────────────────────────────────────

    @Test
    fun `downloadCertificate returns descriptive failure on 403`() = runTest {
        coEvery { api.getCertificate(enrollmentId) } returns
            Response.error(403, "".toResponseBody("text/plain".toMediaType()))

        val result = repository.downloadCertificate(enrollmentId)

        assertFalse(result.isSuccess)
        assertEquals(
            "Cursul nu este completat sau nu ești eligibil pentru certificat",
            result.exceptionOrNull()?.message
        )
    }

    // ── 404 Not Found ────────────────────────────────────────────────────────

    @Test
    fun `downloadCertificate returns descriptive failure on 404`() = runTest {
        coEvery { api.getCertificate(enrollmentId) } returns
            Response.error(404, "".toResponseBody("text/plain".toMediaType()))

        val result = repository.downloadCertificate(enrollmentId)

        assertFalse(result.isSuccess)
        assertEquals("Enrollment-ul nu a fost găsit", result.exceptionOrNull()?.message)
    }

    // ── Other server errors ───────────────────────────────────────────────────

    @Test
    fun `downloadCertificate returns generic failure on 500`() = runTest {
        coEvery { api.getCertificate(enrollmentId) } returns
            Response.error(500, "".toResponseBody("text/plain".toMediaType()))

        val result = repository.downloadCertificate(enrollmentId)

        assertFalse(result.isSuccess)
        assertEquals("Eroare server: 500", result.exceptionOrNull()?.message)
    }

    // ── Network / exception ───────────────────────────────────────────────────

    @Test
    fun `downloadCertificate returns failure on network exception`() = runTest {
        coEvery { api.getCertificate(enrollmentId) } throws RuntimeException("timeout")

        val result = repository.downloadCertificate(enrollmentId)

        assertFalse(result.isSuccess)
        assertEquals("timeout", result.exceptionOrNull()?.message)
    }

    @Test
    fun `downloadCertificate returns failure on IO exception`() = runTest {
        coEvery { api.getCertificate(enrollmentId) } throws java.io.IOException("connection reset")

        val result = repository.downloadCertificate(enrollmentId)

        assertFalse(result.isSuccess)
        assertEquals("connection reset", result.exceptionOrNull()?.message)
    }
}
