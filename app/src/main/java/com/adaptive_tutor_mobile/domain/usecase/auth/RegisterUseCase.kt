package com.adaptive_tutor_mobile.domain.usecase.auth

import com.adaptive_tutor_mobile.data.remote.dto.RegisterRequest
import com.adaptive_tutor_mobile.domain.model.auth.User
import com.adaptive_tutor_mobile.domain.repository.auth.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(request: RegisterRequest): Result<User> =
        repository.register(request)
}
