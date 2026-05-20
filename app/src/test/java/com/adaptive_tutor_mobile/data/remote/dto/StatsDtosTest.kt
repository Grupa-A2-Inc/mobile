package com.adaptive_tutor_mobile.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StatsDtosTest {

    private val attempt = AttemptDetailsDto(
        attemptId = "att-1",
        testId = "test-1",
        testTitle = "Test de baza",
        score = 8.5,
        scorePercent = 85.0,
        passed = true,
        attemptedAt = "2025-05-01T10:00:00"
    )

    private val hardLesson = DifficultyLessonDto(
        lessonId = "lesson-1",
        lessonTitle = "Lectia dificila",
        myBestScore = 40.0,
        classAverage = 75.0,
        gap = 35.0
    )

    private val courseStats = CourseStatsDto(
        courseTitle = "Matematica",
        totalTests = 10,
        totalTestDone = 8,
        passedTests = 6,
        bestScore = 9.5,
        lowestScore = 4.0,
        avgScore = 7.2,
        hardestLessons = listOf(hardLesson),
        lastAttempts = listOf(attempt)
    )

    @Test
    fun `AttemptDetailsDto stores all fields correctly`() {
        assertEquals("att-1", attempt.attemptId)
        assertEquals("test-1", attempt.testId)
        assertEquals("Test de baza", attempt.testTitle)
        assertEquals(8.5, attempt.score, 0.001)
        assertEquals(85.0, attempt.scorePercent, 0.001)
        assertTrue(attempt.passed)
        assertEquals("2025-05-01T10:00:00", attempt.attemptedAt)
    }

    @Test
    fun `AttemptDetailsDto passed can be false`() {
        val failed = attempt.copy(passed = false, scorePercent = 30.0)
        assertFalse(failed.passed)
        assertEquals(30.0, failed.scorePercent, 0.001)
    }

    @Test
    fun `DifficultyLessonDto stores all fields correctly`() {
        assertEquals("lesson-1", hardLesson.lessonId)
        assertEquals("Lectia dificila", hardLesson.lessonTitle)
        assertEquals(40.0, hardLesson.myBestScore, 0.001)
        assertEquals(75.0, hardLesson.classAverage, 0.001)
        assertEquals(35.0, hardLesson.gap, 0.001)
    }

    @Test
    fun `DifficultyLessonDto gap reflects class average minus personal score`() {
        val lesson = DifficultyLessonDto("l1", "L1", myBestScore = 50.0, classAverage = 80.0, gap = 30.0)
        assertEquals(lesson.classAverage - lesson.myBestScore, lesson.gap, 0.001)
    }

    @Test
    fun `CourseStatsDto stores course title and scores`() {
        assertEquals("Matematica", courseStats.courseTitle)
        assertEquals(9.5, courseStats.bestScore, 0.001)
        assertEquals(4.0, courseStats.lowestScore, 0.001)
        assertEquals(7.2, courseStats.avgScore, 0.001)
    }

    @Test
    fun `CourseStatsDto stores test counts`() {
        assertEquals(10, courseStats.totalTests)
        assertEquals(8, courseStats.totalTestDone)
        assertEquals(6, courseStats.passedTests)
    }

    @Test
    fun `CourseStatsDto hardestLessons list accessible`() {
        assertEquals(1, courseStats.hardestLessons.size)
        assertEquals("lesson-1", courseStats.hardestLessons[0].lessonId)
    }

    @Test
    fun `CourseStatsDto lastAttempts list accessible`() {
        assertEquals(1, courseStats.lastAttempts.size)
        assertEquals("att-1", courseStats.lastAttempts[0].attemptId)
    }

    @Test
    fun `CourseStatsDto empty lists`() {
        val stats = courseStats.copy(hardestLessons = emptyList(), lastAttempts = emptyList())
        assertTrue(stats.hardestLessons.isEmpty())
        assertTrue(stats.lastAttempts.isEmpty())
    }

    @Test
    fun `MyTestStatsDto stores all fields correctly`() {
        val dto = MyTestStatsDto(
            testId = "t1",
            testTitle = "Quiz rapid",
            totalAttemptCount = 5,
            bestScore = 10.0,
            lowestScore = 5.0,
            averageScore = 7.5,
            lastScore = 8.0,
            totalStudentCount = 30,
            classAverage = 6.8,
            classMedian = 7.0,
            rank = 3,
            percentile = 90.0
        )
        assertEquals("t1", dto.testId)
        assertEquals("Quiz rapid", dto.testTitle)
        assertEquals(5, dto.totalAttemptCount)
        assertEquals(10.0, dto.bestScore, 0.001)
        assertEquals(5.0, dto.lowestScore, 0.001)
        assertEquals(7.5, dto.averageScore, 0.001)
        assertEquals(8.0, dto.lastScore, 0.001)
        assertEquals(30, dto.totalStudentCount)
        assertEquals(6.8, dto.classAverage, 0.001)
        assertEquals(7.0, dto.classMedian, 0.001)
        assertEquals(3, dto.rank)
        assertEquals(90.0, dto.percentile, 0.001)
    }

    @Test
    fun `MyTestStatsDto copy updates specific field`() {
        val dto = MyTestStatsDto(
            testId = "t1", testTitle = "T", totalAttemptCount = 1,
            bestScore = 9.0, lowestScore = 9.0, averageScore = 9.0,
            lastScore = 9.0, totalStudentCount = 10, classAverage = 7.0,
            classMedian = 7.5, rank = 1, percentile = 99.0
        )
        val updated = dto.copy(rank = 2, percentile = 95.0)
        assertEquals(2, updated.rank)
        assertEquals(95.0, updated.percentile, 0.001)
        assertEquals("t1", updated.testId)
    }

    @Test
    fun `data class equality for AttemptDetailsDto`() {
        val a = attempt.copy()
        assertEquals(attempt, a)
    }
}
