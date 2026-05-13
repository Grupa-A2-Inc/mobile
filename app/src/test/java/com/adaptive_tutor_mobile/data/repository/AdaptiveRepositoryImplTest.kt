package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.AdaptiveApi
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveExerciseStudentDto
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveStartResponseDto
import com.adaptive_tutor_mobile.data.remote.dto.OptionForStudentDto
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response

class AdaptiveRepositoryImplTest {

    private val api: AdaptiveApi = mock()
    private val repository = AdaptiveRepositoryImpl(api)

    @Test
    fun `startSession maps ids and answers into options`() = runTest {
        val exercises = listOf(
            AdaptiveExerciseStudentDto(
                questionId = "123",
                content = "Q1",
                questionType = "SINGLE_CHOICE",
                difficulty = 0.5,
                options = null,
                answers = listOf("A", "B")
            ),
            AdaptiveExerciseStudentDto(
                questionId = null,
                content = "Q2",
                questionType = "MULTI_CHOICE",
                difficulty = 0.7,
                options = listOf(OptionForStudentDto(optionId = 10, text = "X", displayOrder = 0)),
                answers = null
            )
        )
        val responseDto = AdaptiveStartResponseDto(
            sessionId = "s1",
            attemptId = "a1",
            expiresAt = "later",
            exercises = exercises
        )
        whenever(api.startSession(any())).thenReturn(Response.success(responseDto))

        val result = repository.startSession(subjectId = 1, topicId = 2, count = 3)

        assertTrue(result.isSuccess)
        val session = result.getOrThrow()
        assertEquals(2, session.questions.size)
        assertEquals(123, session.questions[0].questionId)
        assertEquals(2, session.questions[0].options?.size)
        assertEquals(0, session.questions[0].options?.first()?.optionId)
        assertEquals(1, session.questions[1].questionId)
        assertEquals(10, session.questions[1].options?.first()?.optionId)
    }

    @Test
    fun `startSession returns failure with parsed error message`() = runTest {
        val body = """{"message":"bad request"}"""
            .toResponseBody("application/json".toMediaType())
        whenever(api.startSession(any())).thenReturn(Response.error(400, body))

        val result = repository.startSession(subjectId = 1, topicId = 2, count = 3)

        assertTrue(result.isFailure)
        assertEquals("bad request", result.exceptionOrNull()?.message)
    }

    @Test
    fun `startSession stores attemptId in session`() = runTest {
        val dto = AdaptiveStartResponseDto(
            sessionId = "sess-1", attemptId = "att-1", expiresAt = null, exercises = emptyList()
        )
        whenever(api.startSession(any())).thenReturn(Response.success(dto))

        val session = repository.startSession(1, 2, 3).getOrThrow()

        assertEquals("sess-1", session.sessionId)
        assertEquals("att-1", session.attemptId)
    }

    @Test
    fun `startSession null attemptId propagated as null`() = runTest {
        val dto = AdaptiveStartResponseDto(
            sessionId = "s2", attemptId = null, expiresAt = null, exercises = emptyList()
        )
        whenever(api.startSession(any())).thenReturn(Response.success(dto))

        val session = repository.startSession(1, 2, 1).getOrThrow()

        assertEquals(null, session.attemptId)
    }

    @Test
    fun `startSession empty exercises returns empty questions list`() = runTest {
        val dto = AdaptiveStartResponseDto("s1", null, null, exercises = emptyList())
        whenever(api.startSession(any())).thenReturn(Response.success(dto))

        val session = repository.startSession(1, 2, 0).getOrThrow()

        assertTrue(session.questions.isEmpty())
    }

    @Test
    fun `startSession null body throws inside Result`() = runTest {
        whenever(api.startSession(any())).thenReturn(Response.success(null))

        val result = repository.startSession(1, 2, 3)

        assertTrue(result.isFailure)
    }

    @Test
    fun `startSession network exception returns failure`() = runTest {
        whenever(api.startSession(any())).thenThrow(RuntimeException("timeout"))

        val result = repository.startSession(1, 2, 3)

        assertTrue(result.isFailure)
        assertEquals("timeout", result.exceptionOrNull()?.message)
    }

    @Test
    fun `startSession 401 uses default message when body blank`() = runTest {
        whenever(api.startSession(any())).thenReturn(
            Response.error(401, "".toResponseBody("application/json".toMediaType()))
        )

        val result = repository.startSession(1, 2, 3)

        assertTrue(result.isFailure)
        assertEquals("Nu ești autentificat", result.exceptionOrNull()?.message)
    }

    @Test
    fun `startSession 404 uses default message when body blank`() = runTest {
        whenever(api.startSession(any())).thenReturn(
            Response.error(404, "".toResponseBody("application/json".toMediaType()))
        )

        val result = repository.startSession(1, 2, 3)

        assertTrue(result.isFailure)
        assertEquals("Sesiunea adaptivă nu a fost găsită", result.exceptionOrNull()?.message)
    }

    @Test
    fun `startSession unknown code uses generic message`() = runTest {
        whenever(api.startSession(any())).thenReturn(
            Response.error(503, "".toResponseBody("application/json".toMediaType()))
        )

        val result = repository.startSession(1, 2, 3)

        assertTrue(result.isFailure)
        assertEquals("Eroare 503", result.exceptionOrNull()?.message)
    }

    @Test
    fun `startSession parses error field when message absent`() = runTest {
        val body = """{"error":"Service unavailable"}"""
            .toResponseBody("application/json".toMediaType())
        whenever(api.startSession(any())).thenReturn(Response.error(503, body))

        val result = repository.startSession(1, 2, 3)

        assertTrue(result.isFailure)
        assertEquals("Service unavailable", result.exceptionOrNull()?.message)
    }

    @Test
    fun `startSession malformed json body falls back to code message`() = runTest {
        whenever(api.startSession(any())).thenReturn(
            Response.error(500, "not-json".toResponseBody("application/json".toMediaType()))
        )

        val result = repository.startSession(1, 2, 3)

        assertTrue(result.isFailure)
        assertEquals("Eroare 500", result.exceptionOrNull()?.message)
    }

    @Test
    fun `startSession uses list index when questionId is non-numeric string`() = runTest {
        val exercises = listOf(
            AdaptiveExerciseStudentDto(
                questionId = "abc",
                content = "Q", questionType = "SINGLE_CHOICE", difficulty = 0.5,
                answers = listOf("X")
            )
        )
        val dto = AdaptiveStartResponseDto("s1", null, null, exercises)
        whenever(api.startSession(any())).thenReturn(Response.success(dto))

        val session = repository.startSession(1, 2, 1).getOrThrow()

        assertEquals(0, session.questions[0].questionId)
    }

    @Test
    fun `startSession both options and answers null results in null options`() = runTest {
        val exercises = listOf(
            AdaptiveExerciseStudentDto(
                questionId = "1", content = "Q", questionType = "SINGLE_CHOICE",
                difficulty = 0.5, options = null, answers = null
            )
        )
        val dto = AdaptiveStartResponseDto("s1", null, null, exercises)
        whenever(api.startSession(any())).thenReturn(Response.success(dto))

        val session = repository.startSession(1, 2, 1).getOrThrow()

        assertEquals(null, session.questions[0].options)
    }
}
