package com.adaptive_tutor_mobile.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.adaptive_tutor_mobile.ui.theme.AdaptiveTutorTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ErrorCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `ErrorCard renders retry button when callback exists`() {
        var retries = 0

        composeRule.setContent {
            AdaptiveTutorTheme {
                ErrorCard(
                    message = "A apărut o eroare",
                    onRetry = { retries++ }
                )
            }
        }

        composeRule.onNodeWithText("A apărut o eroare").assertIsDisplayed()
        composeRule.onNodeWithText("Încearcă din nou").performClick()
        assertEquals(1, retries)
    }

    @Test
    fun `ErrorCard hides retry button when callback missing`() {
        composeRule.setContent {
            AdaptiveTutorTheme {
                ErrorCard(
                    message = "A apărut o eroare",
                    modifier = Modifier
                )
            }
        }

        composeRule.onNodeWithText("A apărut o eroare").assertIsDisplayed()
        composeRule.onAllNodesWithText("Încearcă din nou").assertCountEquals(0)
    }
}
