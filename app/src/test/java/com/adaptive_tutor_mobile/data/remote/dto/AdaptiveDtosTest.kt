package com.adaptive_tutor_mobile.data.remote.dto

import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class AdaptiveDtosTest {

    @Test
    fun `adaptive start dto stores request and response values`() {
        val request = AdaptiveStartRequestDto(
            subjectId = 1,
            topicId = 2,
            count = 3
        )
        val exercise = AdaptiveExerciseStudentDto(
            questionId = "q1",
            content = "Question",
            questionType = "SINGLE_CHOICE",
            difficulty = 0.7,
            options = listOf(OptionForStudentDto(1, "A", 0)),
            answers = listOf("A")
        )
        val response = AdaptiveStartResponseDto(
            sessionId = "session-1",
            attemptId = "attempt-1",
            expiresAt = "2026-01-01T00:00:00Z",
            exercises = listOf(exercise)
        )

        assertEquals(1, request.subjectId)
        assertEquals(2, request.topicId)
        assertEquals(3, request.count)
        assertEquals("session-1", response.sessionId)
        assertEquals("attempt-1", response.attemptId)
        assertEquals("2026-01-01T00:00:00Z", response.expiresAt)
        assertEquals("q1", response.exercises?.first()?.questionId)
        assertEquals(listOf("A"), response.exercises?.first()?.answers)
    }

    @Test
    fun `adaptive dto optional fields support defaults`() {
        val response = AdaptiveStartResponseDto(
            sessionId = "session-2",
            expiresAt = null
        )
        val exercise = AdaptiveExerciseStudentDto()

        assertNull(response.attemptId)
        assertNull(response.exercises)
        assertNull(exercise.questionId)
        assertNull(exercise.content)
        assertNull(exercise.questionType)
        assertNull(exercise.difficulty)
        assertNull(exercise.options)
        assertNull(exercise.answers)
    }
}
