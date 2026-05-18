package com.adaptive_tutor_mobile.domain.repository

import com.adaptive_tutor_mobile.domain.model.AttemptHistory
import com.adaptive_tutor_mobile.domain.model.BestAttempt

interface AttemptHistoryRepository {
    suspend fun getAttempts(testId: String): Result<List<AttemptHistory>>
    suspend fun getBestAttempt(testId: String): Result<BestAttempt>
}
