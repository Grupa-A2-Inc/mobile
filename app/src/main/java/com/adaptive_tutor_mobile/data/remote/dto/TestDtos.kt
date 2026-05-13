package com.adaptive_tutor_mobile.data.remote.dto

data class SubmitAnswersRequest(val answers: Map<Int, List<Int>>)
data class TestAttemptDto(val attemptId: String, val timeLimitSec: Int, val questions: List<QuestionDto>)
data class QuestionDto(val id: Int, val type: String, val content: String, val options: List<OptionDto>)
data class OptionDto(val id: Int, val text: String)
data class AttemptResultDto(val score: Int, val scorePercent: Int, val passed: Boolean, val questions: List<QuestionResultDto>)
data class QuestionResultDto(val question: QuestionDto, val correctOptionIds: List<Int>, val userOptionIds: List<Int>)