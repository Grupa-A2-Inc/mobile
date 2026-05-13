package com.adaptive_tutor_mobile.domain.repository

import com.adaptive_tutor_mobile.domain.model.LessonDetail

fun interface LessonRepository {
    suspend fun getLessonDetail(lessonId: String): Result<LessonDetail>
}