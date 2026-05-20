package com.adaptive_tutor_mobile.domain.model.lesson

import com.adaptive_tutor_mobile.domain.model.courses.LessonSummary

data class LessonWithContent(
    val summary: LessonSummary,
    val contentMarkdown: String
)
