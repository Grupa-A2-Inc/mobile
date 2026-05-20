package com.adaptive_tutor_mobile.domain.usecase.lesson

import com.adaptive_tutor_mobile.domain.model.lesson.LessonDetail
import com.adaptive_tutor_mobile.domain.repository.lesson.LessonRepository
import javax.inject.Inject

class GetLessonDetailUseCase @Inject constructor(
    private val repository: LessonRepository
) {
    suspend operator fun invoke(lessonId: String): Result<LessonDetail> {
        return repository.getLessonDetail(lessonId)
    }
}
