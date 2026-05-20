package com.adaptive_tutor_mobile.domain.model

import com.adaptive_tutor_mobile.domain.model.lesson.LessonDetail
import com.adaptive_tutor_mobile.domain.model.lesson.LessonResource
import com.adaptive_tutor_mobile.domain.model.test.AttemptResult
import com.adaptive_tutor_mobile.domain.model.test.Option
import com.adaptive_tutor_mobile.domain.model.test.Question
import com.adaptive_tutor_mobile.domain.model.test.QuestionResult
import com.adaptive_tutor_mobile.domain.model.test.QuestionType
import com.adaptive_tutor_mobile.domain.model.test.TestAttempt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainModelSmokeTest {

    @Test
    fun `lesson detail and resources hold data`() {
        val resource = LessonResource(id = "r1", title = "Doc", url = "https://example.com")
        val detail = LessonDetail(
            id = "l1",
            title = "Lesson",
            contentMarkdown = "# Content",
            resources = listOf(resource)
        )

        assertEquals("l1", detail.id)
        assertEquals("Lesson", detail.title)
        assertEquals(1, detail.resources.size)
        assertEquals("Doc", detail.resources.first().title)
    }

    @Test
    fun `test attempt model holds questions and results`() {
        val options = listOf(Option(id = 1, text = "A"), Option(id = 2, text = "B"))
        val question = Question(id = 10, type = QuestionType.SINGLE_CHOICE, content = "Q?", options = options)
        val attempt = TestAttempt(attemptId = "a1", timeLimitSec = 60, questions = listOf(question))

        val result = QuestionResult(question = question, correctOptionIds = listOf(1), userOptionIds = listOf(2))
        val report = AttemptResult(score = 1, scorePercent = 50, passed = false, questions = listOf(result))

        assertEquals("a1", attempt.attemptId)
        assertEquals(1, attempt.questions.size)
        assertEquals(50, report.scorePercent)
        assertTrue(report.questions.first().userOptionIds.contains(2))
    }
}
