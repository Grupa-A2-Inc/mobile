package com.adaptive_tutor_mobile.domain.usecase.profile

import com.adaptive_tutor_mobile.domain.repository.profile.UserRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        userId: String,
        email: String,
        firstName: String,
        lastName: String,
        organizationId: String?
    ) =
        userRepository.updateProfile(
            userId,
            email,
            firstName,
            lastName,
            organizationId
        )
}
