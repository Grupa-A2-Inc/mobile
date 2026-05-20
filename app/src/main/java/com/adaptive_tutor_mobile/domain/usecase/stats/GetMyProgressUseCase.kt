package com.adaptive_tutor_mobile.domain.usecase.stats

import com.adaptive_tutor_mobile.data.remote.dto.ProgressWithLessonListDto
import com.adaptive_tutor_mobile.domain.repository.stats.ProgressRepository
import javax.inject.Inject

class GetMyProgressUseCase @Inject constructor(
    private val repository: ProgressRepository
) {
    suspend operator fun invoke(courseId: String): Result<ProgressWithLessonListDto> =
        repository.getMyProgress(courseId)
}
