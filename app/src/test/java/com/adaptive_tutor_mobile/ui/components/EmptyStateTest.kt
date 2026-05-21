package com.adaptive_tutor_mobile.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
class EmptyStateTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `EmptyState renders subtitle and action when both are present`() {
        var clicks = 0

        composeRule.setContent {
            AdaptiveTutorTheme {
                EmptyState(
                    icon = Icons.Filled.Info,
                    title = "Niciun curs",
                    subtitle = "Nu există încă înscrieri",
                    actionText = "Explorează",
                    onAction = { clicks++ }
                )
            }
        }

        composeRule.onNodeWithText("Niciun curs").assertIsDisplayed()
        composeRule.onNodeWithText("Nu există încă înscrieri").assertIsDisplayed()
        composeRule.onNodeWithText("Explorează").performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `EmptyState hides optional content when subtitle and action are absent`() {
        composeRule.setContent {
            AdaptiveTutorTheme {
                EmptyState(
                    icon = Icons.Filled.Info,
                    title = "Gol",
                    subtitle = null,
                    actionText = null,
                    onAction = null
                )
            }
        }

        composeRule.onNodeWithText("Gol").assertIsDisplayed()
        composeRule.onAllNodesWithText("Explorează").assertCountEquals(0)
    }

    @Test
    fun `EmptyState does not render action when callback is missing`() {
        composeRule.setContent {
            AdaptiveTutorTheme {
                EmptyState(
                    icon = Icons.Filled.Info,
                    title = "Gol",
                    subtitle = "",
                    actionText = "Explorează",
                    onAction = null
                )
            }
        }

        composeRule.onNodeWithText("Gol").assertIsDisplayed()
        composeRule.onAllNodesWithText("Explorează").assertCountEquals(0)
    }
}
