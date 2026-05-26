package com.adaptive_tutor_mobile.domain.usecase.auth

import com.adaptive_tutor_mobile.di.SessionStore
import com.adaptive_tutor_mobile.domain.model.auth.UserRole
import javax.inject.Inject

class CheckUserRoleUseCase @Inject constructor(
    private val sessionStore: SessionStore
) {
    suspend operator fun invoke(): UserRole =
        sessionStore.getUser()?.role ?: UserRole.UNKNOWN
}
