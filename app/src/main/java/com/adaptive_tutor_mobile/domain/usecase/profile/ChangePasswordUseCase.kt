package com.adaptive_tutor_mobile.domain.usecase.profile

import com.adaptive_tutor_mobile.domain.repository.profile.UserRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        userId: String,
        currentPassword: String,
        newPassword: String,
        newPasswordConfirm: String
    ) =
        userRepository.changePassword(
            userId,
            currentPassword,
            newPassword,
            newPasswordConfirm
        )
}
