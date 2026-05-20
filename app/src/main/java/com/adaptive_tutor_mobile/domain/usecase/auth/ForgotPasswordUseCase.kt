package com.adaptive_tutor_mobile.domain.usecase.auth

import com.adaptive_tutor_mobile.domain.repository.auth.AuthRepository
import javax.inject.Inject

class ForgotPasswordUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> = repository.forgotPassword(email)
}
