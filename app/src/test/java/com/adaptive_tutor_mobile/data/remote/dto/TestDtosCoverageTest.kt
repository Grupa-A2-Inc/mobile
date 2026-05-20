package com.adaptive_tutor_mobile.data.remote.dto

import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class TestDtosCoverageTest {

    @Test
    fun `teacher side test dto models store constructor values`() {
        val testEdit = TestEditDto("Title", "Description", 600, true)
        val optionRequest = OptionRequestDto("Answer", 1, true)
        val questionRequest = QuestionRequestDto("SINGLE_CHOICE", "Question", 0.6, listOf(optionRequest))
        val optionResponse = OptionResponseDto(1L, "Answer", 1, true)
        val questionResponse = QuestionResponseDto(2L, "MULTI_CHOICE", "Question", 0.8, listOf(optionResponse))
        val status = AttemptStatusDto("attempt-1", 2, 90.0, 90.0, true, "2026-01-01T00:00:00Z", "DONE")

        assertEquals("Title", testEdit.title)
        assertEquals("Answer", optionRequest.text)
        assertEquals("Question", questionRequest.content)
        assertEquals(1L, optionResponse.optionId)
        assertEquals("MULTI_CHOICE", questionResponse.questionType)
        assertEquals("DONE", status.status)
    }

    @Test
    fun `test dto nullable fields preserve nulls`() {
        val testEdit = TestEditDto(null, null, null, null)
        val optionResponse = OptionResponseDto(3L, "Maybe", 2, null)
        val questionResponse = QuestionResponseDto(4L, "TRUE_FALSE", "Prompt", null, listOf(optionResponse))

        assertNull(testEdit.title)
        assertNull(testEdit.aiEnabled)
        assertNull(optionResponse.isCorrect)
        assertNull(questionResponse.difficulty)
    }

    @Test
    fun `student and report dto models support default optional arguments`() {
        val studentQuestion = QuestionForStudentDto(
            questionId = 10,
            difficulty = null
        )
        val startAttempt = StartAttemptResponseDto(
            attemptId = "attempt-1",
            attemptNumber = 1,
            startedAt = "2026-01-01T00:00:00Z",
            timeLimitSec = null,
            test = TestInfoForAttemptDto("test-1", "Sample")
        )
        val report = AttemptReportDTO(
            attemptId = "attempt-1",
            score = null,
            scorePercent = null,
            passed = null,
            completedAt = null
        )
        val questionReport = QuestionForAttemptReportDTO(questionId = 99)

        assertNull(studentQuestion.questionType)
        assertNull(studentQuestion.content)
        assertNull(studentQuestion.options)
        assertNull(startAttempt.questions)
        assertNull(report.questions)
        assertNull(questionReport.questionType)
        assertNull(questionReport.content)
        assertNull(questionReport.selectedOptionIds)
        assertNull(questionReport.correctOptionIds)
        assertEquals(false, questionReport.correct)
    }
}
