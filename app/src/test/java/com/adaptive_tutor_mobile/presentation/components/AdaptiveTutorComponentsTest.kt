package com.adaptive_tutor_mobile.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import com.adaptive_tutor_mobile.ui.theme.SuccessColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
class AdaptiveTutorComponentsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `CourseCard renders optional fields and click`() {
        var clicks = 0

        composeRule.setContent {
            AdaptiveTutorTheme {
                CourseCard(
                    title = "Matematică",
                    description = "Descriere curs",
                    category = "STEM",
                    progressPercent = 42.5,
                    onClick = { clicks++ }
                )
            }
        }

        composeRule.onNodeWithText("Matematică").assertIsDisplayed()
        composeRule.onNodeWithText("Descriere curs").assertIsDisplayed()
        composeRule.onNodeWithText("STEM").assertIsDisplayed()
        composeRule.onNodeWithText("42%").assertIsDisplayed()
        composeRule.onNodeWithText("Progres").performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `CourseCard hides blank optional fields`() {
        composeRule.setContent {
            AdaptiveTutorTheme {
                CourseCard(
                    title = "Fizică",
                    description = " ",
                    category = "",
                    progressPercent = null,
                    onClick = {}
                )
            }
        }

        composeRule.onNodeWithText("Fizică").assertIsDisplayed()
        composeRule.onAllNodesWithText("Progres").assertCountEquals(0)
    }

    @Test
    fun `LessonCard shows visited state and badge`() {
        var clicks = 0

        composeRule.setContent {
            AdaptiveTutorTheme {
                LessonCard(
                    title = "Lecția 1",
                    orderIndex = 1,
                    visited = true,
                    hasTest = true,
                    onClick = { clicks++ }
                )
            }
        }

        composeRule.onNodeWithText("1. Lecția 1").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Vizitat").assertIsDisplayed()
        composeRule.onNodeWithText("Test").assertIsDisplayed()
        composeRule.onNodeWithText("1. Lecția 1").performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `LessonCard shows unvisited state without badge`() {
        composeRule.setContent {
            AdaptiveTutorTheme {
                LessonCard(
                    title = "Lecția 2",
                    orderIndex = 2,
                    visited = false,
                    hasTest = false,
                    onClick = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("Nevizitat").assertIsDisplayed()
        composeRule.onAllNodesWithText("Test").assertCountEquals(0)
    }

    @Test
    fun `loading error and empty screens render expected content`() {
        var retries = 0

        composeRule.setContent {
            AdaptiveTutorTheme {
                LoadingScreen()
                ErrorScreen(message = "Eroare server", onRetry = { retries++ })
                EmptyScreen(message = "Nimic aici", icon = Icons.Filled.Info)
            }
        }

        composeRule.onNodeWithText("Se încarcă...").assertIsDisplayed()
        composeRule.onNodeWithText("Eroare server").assertIsDisplayed()
        composeRule.onNodeWithText("Reîncearcă").performClick()
        composeRule.onNodeWithText("Nimic aici").assertIsDisplayed()
        assertEquals(1, retries)
    }

    @Test
    fun `AdaptiveTopBar renders subtitle back and action`() {
        var backClicks = 0
        var actionClicks = 0

        composeRule.setContent {
            AdaptiveTutorTheme {
                AdaptiveTopBar(
                    title = "Catalog",
                    subtitle = "2 cursuri",
                    onBack = { backClicks++ },
                    actions = {
                        IconButton(onClick = { actionClicks++ }) {
                            Icon(Icons.Filled.Add, contentDescription = "Adaugă")
                        }
                    }
                )
            }
        }

        composeRule.onNodeWithText("Catalog").assertIsDisplayed()
        composeRule.onNodeWithText("2 cursuri").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Înapoi").performClick()
        composeRule.onNodeWithContentDescription("Adaugă").performClick()
        assertEquals(1, backClicks)
        assertEquals(1, actionClicks)
    }

    @Test
    fun `AdaptiveTopBar hides subtitle when blank and back when missing`() {
        composeRule.setContent {
            AdaptiveTutorTheme {
                AdaptiveTopBar(
                    title = "Acasă",
                    subtitle = " "
                )
            }
        }

        composeRule.onNodeWithText("Acasă").assertIsDisplayed()
        composeRule.onAllNodesWithContentDescription("Înapoi").assertCountEquals(0)
    }

    @Test
    fun `AdaptiveBottomBar renders items and dispatches click`() {
        var selectedRoute: String? = null
        val items = listOf(
            BottomNavItem("home", Icons.Filled.Info, "Acasă"),
            BottomNavItem("courses", Icons.Filled.MenuBook, "Cursuri")
        )

        composeRule.setContent {
            AdaptiveTutorTheme {
                AdaptiveBottomBar(
                    items = items,
                    currentRoute = "home",
                    onItemClick = { selectedRoute = it }
                )
            }
        }

        composeRule.onNodeWithText("Acasă").assertIsDisplayed()
        composeRule.onNodeWithText("Cursuri").performClick()
        assertEquals("courses", selectedRoute)
    }

    @Test
    fun `StatusChip and ScoreCircle render both pass states`() {
        composeRule.setContent {
            AdaptiveTutorTheme {
                StatusChip(text = "Activ", color = SuccessColor)
                ScoreCircle(scorePercent = 88.0, passed = true)
                ScoreCircle(scorePercent = 35.0, passed = false)
            }
        }

        composeRule.onNodeWithText("Activ").assertIsDisplayed()
        composeRule.onNodeWithText("88%").assertIsDisplayed()
        composeRule.onNodeWithText("Promovat").assertIsDisplayed()
        composeRule.onNodeWithText("35%").assertIsDisplayed()
        composeRule.onNodeWithText("Nepromovat").assertIsDisplayed()
    }
}
