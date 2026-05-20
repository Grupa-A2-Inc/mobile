package com.adaptive_tutor_mobile.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Test

class StatsDtosTest {

    @Test
    fun `MyTestStatsDto holds correct values`() {
        val dto = MyTestStatsDto(
            testId = "t1",
            testTitle = "Test Title",
            totalAttemptCount = 5,
            bestScore = 90.0,
            lowestScore = 40.0,
            averageScore = 70.0,
            lastScore = 80.0,
            totalStudentCount = 30,
            classAverage = 65.0,
            classMedian = 68.0,
            rank = 3,
            percentile = 90.0
        )

        assertEquals("t1", dto.testId)
        assertEquals("Test Title", dto.testTitle)
        assertEquals(5, dto.totalAttemptCount)
        assertEquals(90.0, dto.bestScore, 0.0)
        assertEquals(3, dto.rank)
        assertEquals(90.0, dto.percentile, 0.0)
    }

    @Test
    fun `CourseStatsDto holds correct values`() {
        val dto = CourseStatsDto(
            courseTitle = "Math",
            totalTests = 10,
            totalTestDone = 8,
            passedTests = 6,
            bestScore = 95.0,
            lowestScore = 50.0,
            avgScore = 75.0,
            hardestLessons = emptyList(),
            lastAttempts = emptyList()
        )

        assertEquals("Math", dto.courseTitle)
        assertEquals(10, dto.totalTests)
        assertEquals(6, dto.passedTests)
    }

    @Test
    fun `DifficultyLessonDto holds correct values`() {
        val dto = DifficultyLessonDto(
            lessonId = "l1",
            lessonTitle = "Algebra",
            myBestScore = 60.0,
            classAverage = 75.0,
            gap = -15.0
        )

        assertEquals("l1", dto.lessonId)
        assertEquals(-15.0, dto.gap, 0.0)
    }

    @Test
    fun `AttemptDetailsDto holds correct values`() {
        val dto = AttemptDetailsDto(
            attemptId = "a1",
            testId = "t1",
            testTitle = "Test",
            score = 80.0,
            scorePercent = 80.0,
            passed = true,
            attemptedAt = "2026-01-01"
        )

        assertEquals("a1", dto.attemptId)
        assertEquals(true, dto.passed)
    }
}