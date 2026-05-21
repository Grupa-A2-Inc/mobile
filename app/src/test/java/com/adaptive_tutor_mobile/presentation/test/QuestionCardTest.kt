package com.adaptive_tutor_mobile.presentation.test

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import com.adaptive_tutor_mobile.domain.model.test.Option
import com.adaptive_tutor_mobile.domain.model.test.Question
import com.adaptive_tutor_mobile.domain.model.test.QuestionType
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QuestionCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val options = listOf(
        Option(id = 1, text = "True"),
        Option(id = 2, text = "False")
    )

    @Test
    fun `QuestionCard single choice sends selected option`() {
        var selected = emptyList<Int>()

        composeRule.setContent {
            QuestionCard(
                question = Question(
                    id = 1,
                    type = QuestionType.SINGLE_CHOICE,
                    content = "Choose one",
                    options = options
                ),
                selectedOptionIds = selected,
                onAnswerSelected = { selected = it }
            )
        }

        composeRule.onAllNodes(isSelectable())[0].performClick()

        assertEquals(listOf(1), selected)
    }

    @Test
    fun `QuestionCard true false uses the same single selection branch`() {
        var selected = emptyList<Int>()

        composeRule.setContent {
            QuestionCard(
                question = Question(
                    id = 2,
                    type = QuestionType.TRUE_FALSE,
                    content = "Earth is round",
                    options = options
                ),
                selectedOptionIds = selected,
                onAnswerSelected = { selected = it }
            )
        }

        composeRule.onAllNodes(isSelectable())[1].performClick()

        assertEquals(listOf(2), selected)
    }

    @Test
    fun `QuestionCard multi choice adds selections`() {
        val selectionHistory = mutableListOf<List<Int>>()

        composeRule.setContent {
            var selected by mutableStateOf(emptyList<Int>())

            QuestionCard(
                question = Question(
                    id = 3,
                    type = QuestionType.MULTI_CHOICE,
                    content = "Pick all valid answers",
                    options = options
                ),
                selectedOptionIds = selected,
                onAnswerSelected = {
                    selected = it
                    selectionHistory += it
                }
            )
        }

        composeRule.onAllNodesWithText("True").assertCountEquals(1)
        composeRule.onAllNodesWithText("False").assertCountEquals(1)

        composeRule.onAllNodes(isToggleable())[0].performClick()

        composeRule.runOnIdle {
            assertEquals(1, selectionHistory.size)
            assertEquals(1, selectionHistory.first().size)
        }
    }
}
