package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.UserApi
import com.adaptive_tutor_mobile.data.remote.dto.ChangePasswordDto
import com.adaptive_tutor_mobile.data.remote.dto.UpdateUserDto
import com.adaptive_tutor_mobile.domain.model.profile.UserProfile
import com.adaptive_tutor_mobile.domain.repository.profile.UserRepository
import com.google.gson.JsonParser
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi
) : UserRepository {

    companion object {
        private const val ERR_UNAUTHORIZED = "Sesiune expirată, te rugăm să te autentifici din nou"
        private const val ERR_FORBIDDEN    = "Acces interzis"
        private const val ERR_NOT_FOUND    = "Utilizatorul nu a fost găsit"
    }

    private fun parseError(errorBody: String?, fallback: String): String {
        if (!errorBody.isNullOrBlank()) {
            return try {
                val json = JsonParser.parseString(errorBody).asJsonObject
                json["message"]?.asString
                    ?: json["error"]?.asString
                    ?: fallback
            } catch (_: Exception) {
                fallback
            }
        }
        return fallback
    }

    private fun mapDto(dto: com.adaptive_tutor_mobile.data.remote.dto.UserProfileDto) = UserProfile(
        id               = dto.id,
        firstName        = dto.firstName,
        lastName         = dto.lastName,
        email            = dto.email,
        organizationName = dto.organizationName,
        country          = dto.country,
        city             = dto.city
    )

    override suspend fun getProfile(userId: String): Result<UserProfile> {
        return try {
            val response = userApi.getUserById(userId)
            if (response.isSuccessful) {
                val dto = response.body()
                    ?: return Result.failure(Exception("Răspuns gol de la server"))
                Result.success(mapDto(dto))
            } else {
                val fallback = when (response.code()) {
                    401  -> ERR_UNAUTHORIZED
                    403  -> ERR_FORBIDDEN
                    404  -> ERR_NOT_FOUND
                    else -> "Eroare la încărcarea profilului (${response.code()})"
                }
                Result.failure(
                    Exception(parseError(response.errorBody()?.string(), fallback))
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(
        userId: String,
        email: String,
        firstName: String,
        lastName: String,
        organizationId: String?
    ): Result<UserProfile> {
        return try {
            val response = userApi.updateUser(
                userId,
                UpdateUserDto(
                    email          = email,
                    firstName      = firstName,
                    lastName       = lastName,
                    organizationId = organizationId
                )
            )
            if (response.isSuccessful) {
                val dto = response.body()
                Result.success(
                    if (dto != null) mapDto(dto)
                    else UserProfile(
                        id               = userId,
                        firstName        = firstName,
                        lastName         = lastName,
                        email            = email,
                        organizationName = null,
                        country          = null,
                        city             = null
                    )
                )
            } else {
                val fallback = when (response.code()) {
                    400  -> "Date invalide"
                    401  -> ERR_UNAUTHORIZED
                    403  -> ERR_FORBIDDEN
                    404  -> ERR_NOT_FOUND
                    409  -> "Emailul este deja folosit de un alt cont"
                    else -> "Eroare la salvarea profilului (${response.code()})"
                }
                Result.failure(
                    Exception(parseError(response.errorBody()?.string(), fallback))
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePassword(
        userId: String,
        currentPassword: String,
        newPassword: String,
        newPasswordConfirm: String
    ): Result<Unit> {
        return try {
            val response = userApi.changePassword(
                userId,
                ChangePasswordDto(
                    currentPassword    = currentPassword,
                    newPassword        = newPassword,
                    newPasswordConfirm = newPasswordConfirm
                )
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val fallback = when (response.code()) {
                    400  -> "Parola curentă este incorectă"
                    401  -> ERR_UNAUTHORIZED
                    403  -> ERR_FORBIDDEN
                    404  -> ERR_NOT_FOUND
                    422  -> "Parola nouă nu respectă cerințele de securitate"
                    else -> "Eroare la schimbarea parolei (${response.code()})"
                }
                Result.failure(
                    Exception(parseError(response.errorBody()?.string(), fallback))
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
