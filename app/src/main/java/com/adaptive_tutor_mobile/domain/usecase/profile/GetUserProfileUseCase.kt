package com.adaptive_tutor_mobile.domain.usecase.profile

import com.adaptive_tutor_mobile.domain.repository.profile.UserRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String) = userRepository.getProfile(userId)
}
