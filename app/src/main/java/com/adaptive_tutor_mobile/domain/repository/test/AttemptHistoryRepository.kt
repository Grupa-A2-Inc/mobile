package com.adaptive_tutor_mobile.domain.repository.test

import com.adaptive_tutor_mobile.domain.model.test.AttemptHistory
import com.adaptive_tutor_mobile.domain.model.test.BestAttempt

interface AttemptHistoryRepository {
    suspend fun getAttempts(testId: String): Result<List<AttemptHistory>>
    suspend fun getBestAttempt(testId: String): Result<BestAttempt>
}
