package com.adaptive_tutor_mobile.domain.usecase.auth

import com.adaptive_tutor_mobile.domain.repository.auth.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(): Result<Unit> = repository.logout()
}
