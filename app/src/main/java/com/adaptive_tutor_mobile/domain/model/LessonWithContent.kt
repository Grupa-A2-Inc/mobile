package com.adaptive_tutor_mobile.domain.model

data class LessonWithContent(
    val summary: LessonSummary,
    val contentMarkdown: String
)