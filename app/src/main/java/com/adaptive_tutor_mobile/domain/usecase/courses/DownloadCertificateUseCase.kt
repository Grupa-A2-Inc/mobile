package com.adaptive_tutor_mobile.domain.usecase.courses

import com.adaptive_tutor_mobile.domain.repository.courses.CertificateRepository
import javax.inject.Inject

class DownloadCertificateUseCase @Inject constructor(
    private val repository: CertificateRepository
) {
    suspend operator fun invoke(enrollmentId: String): Result<ByteArray> =
        repository.downloadCertificate(enrollmentId)
}
