package com.adaptive_tutor_mobile.presentation.adaptive

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SubjectTopicDataTest {

    @Test
    fun `subject and topic lists are populated and consistent`() {
        assertTrue(SUBJECT_LIST.isNotEmpty())
        assertTrue(TOPIC_LIST.isNotEmpty())

        val subjectIds = SUBJECT_LIST.map { it.id }.toSet()
        assertTrue(TOPIC_LIST.all { it.subjectId in subjectIds })

        assertNotNull(SUBJECT_LIST.find { it.id == 2 })
        assertNotNull(TOPIC_LIST.find { it.id == 1001 })
    }

    @Test
    fun `topics cover multiple grades`() {
        val grades = TOPIC_LIST.map { it.grade }.toSet()
        assertTrue(grades.contains(9))
        assertTrue(grades.contains(12))
    }
}
