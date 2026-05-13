package com.adaptive_tutor_mobile.domain.usecase

import com.adaptive_tutor_mobile.domain.model.LessonDetail
import com.adaptive_tutor_mobile.domain.repository.LessonRepository
import javax.inject.Inject

class GetLessonDetailUseCase @Inject constructor(
    private val repository: LessonRepository
) {
    suspend operator fun invoke(lessonId: String): Result<LessonDetail> {
        return repository.getLessonDetail(lessonId)
    }
}