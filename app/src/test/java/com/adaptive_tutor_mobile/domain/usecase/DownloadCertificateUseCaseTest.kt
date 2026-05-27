package com.adaptive_tutor_mobile.domain.usecase

import com.adaptive_tutor_mobile.domain.repository.courses.CertificateRepository
import com.adaptive_tutor_mobile.domain.usecase.courses.DownloadCertificateUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DownloadCertificateUseCaseTest {

    private lateinit var repository: CertificateRepository
    private lateinit var useCase: DownloadCertificateUseCase

    private val enrollmentId = "enr-xyz-456"
    private val pdfBytes = byteArrayOf(0x25, 0x50, 0x44, 0x46)

    @Before
    fun setUp() {
        repository = mockk()
        useCase = DownloadCertificateUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository and returns success`() = runTest {
        coEvery { repository.downloadCertificate(enrollmentId) } returns Result.success(pdfBytes)

        val result = useCase(enrollmentId)

        assertTrue(result.isSuccess)
        assertContentEquals(pdfBytes, result.getOrNull())
        coVerify(exactly = 1) { repository.downloadCertificate(enrollmentId) }
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        val error = IllegalStateException("Nu ești eligibil pentru certificat")
        coEvery { repository.downloadCertificate(enrollmentId) } returns Result.failure(error)

        val result = useCase(enrollmentId)

        assertFalse(result.isSuccess)
        assertEquals(error.message, result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke passes enrollmentId correctly to repository`() = runTest {
        val otherId = "other-enr-999"
        coEvery { repository.downloadCertificate(otherId) } returns Result.success(pdfBytes)

        useCase(otherId)

        coVerify { repository.downloadCertificate(otherId) }
    }
}
