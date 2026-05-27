package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.CertificateApi
import com.adaptive_tutor_mobile.domain.repository.courses.CertificateRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CertificateRepositoryImpl @Inject constructor(
    private val certificateApi: CertificateApi
) : CertificateRepository {

    override suspend fun downloadCertificate(enrollmentId: String): Result<ByteArray> = runCatching {
        val response = certificateApi.getCertificate(enrollmentId)
        when {
            response.isSuccessful -> {
                response.body()?.bytes()
                    ?: error("Răspuns gol de la server")
            }
            response.code() == 403 -> error("Cursul nu este completat sau nu ești eligibil pentru certificat")
            response.code() == 404 -> error("Enrollment-ul nu a fost găsit")
            else -> error("Eroare server: ${response.code()}")
        }
    }
}
