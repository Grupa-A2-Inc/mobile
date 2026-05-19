package com.adaptive_tutor_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CourseStatsDto(
    val courseTitle: String,
    @SerializedName("totalTestCount")
    val totalTests: Int,
    @SerializedName("totalTestDone")
    val totalTestDone: Int,
    @SerializedName("totalTestPassed")
    val passedTests: Int,
    val bestScore: Double,
    val lowestScore: Double,
    @SerializedName("averageScore")
    val avgScore: Double,
    @SerializedName("difficultyLessons")
    val hardestLessons: List<DifficultyLessonDto>,
    @SerializedName("lastAttempts")
    val lastAttempts: List<AttemptDetailsDto>
)

data class DifficultyLessonDto(
    val lessonId: String,
    val lessonTitle: String,
    val myBestScore: Double,
    val classAverage: Double,
    val gap: Double
)

data class AttemptDetailsDto(
    val attemptId: String,
    val testId: String,
    val testTitle: String,
    val score: Double,
    val scorePercent: Double,
    val passed: Boolean,
    @SerializedName("completedAt")
    val attemptedAt: String
)

data class MyTestStatsDto(
    val testId: String,
    val testTitle: String,
    val totalAttemptCount: Int,
    val bestScore: Double,
    val lowestScore: Double,
    val averageScore: Double,
    val lastScore: Double,
    val totalStudentCount: Int,
    val classAverage: Double,
    val classMedian: Double,
    val rank: Int,
    val percentile: Double
)
