package com.adaptive_tutor_mobile.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
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
class AppTopBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `AppTopBar renders title back button and actions`() {
        var backClicks = 0
        var actionClicks = 0

        composeRule.setContent {
            AdaptiveTutorTheme {
                AppTopBar(
                    title = "Cursurile mele",
                    onBack = { backClicks++ },
                    actions = {
                        IconButton(onClick = { actionClicks++ }) {
                            Icon(Icons.Filled.Add, contentDescription = "Adaugă")
                        }
                    }
                )
            }
        }

        composeRule.onNodeWithText("Cursurile mele").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Înapoi").performClick()
        composeRule.onNodeWithContentDescription("Adaugă").performClick()

        assertEquals(1, backClicks)
        assertEquals(1, actionClicks)
    }

    @Test
    fun `AppTopBar hides back button when callback missing`() {
        composeRule.setContent {
            AdaptiveTutorTheme {
                AppTopBar(
                    title = "Acasă",
                    modifier = Modifier,
                    actions = {}
                )
            }
        }

        composeRule.onNodeWithText("Acasă").assertIsDisplayed()
        composeRule.onAllNodesWithContentDescription("Înapoi").assertCountEquals(0)
    }

    @Test
    fun `AppTopBar supports default parameters`() {
        composeRule.setContent {
            AdaptiveTutorTheme {
                AppTopBar(title = "Implicit")
            }
        }

        composeRule.onNodeWithText("Implicit").assertIsDisplayed()
        composeRule.onAllNodesWithContentDescription("Înapoi").assertCountEquals(0)
    }
}
