package com.adaptive_tutor_mobile.domain.repository.courses

fun interface CertificateRepository {
    /**
     * Descarcă certificatul PDF pentru enrollment-ul dat.
     * Returnează byte-urile PDF-ului sau un eșec cu mesaj descriptiv.
     */
    suspend fun downloadCertificate(enrollmentId: String): Result<ByteArray>
}
