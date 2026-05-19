package com.adaptive_tutor_mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import kotlin.test.assertEquals
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ThemeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @After
    fun resetQualifiers() {
        RuntimeEnvironment.setQualifiers("")
    }

    @Test
    fun `AdaptiveTutorTheme uses dark palette for dark mode`() {
        var primary by mutableStateOf(Primary)

        composeRule.setContent {
            AdaptiveTutorTheme(themeMode = "dark") {
                primary = MaterialTheme.colorScheme.primary
            }
        }

        composeRule.runOnIdle { assertEquals(PrimaryDark, primary) }
    }

    @Test
    fun `AdaptiveTutorTheme uses light palette for light mode`() {
        var primary by mutableStateOf(PrimaryDark)

        composeRule.setContent {
            AdaptiveTutorTheme(themeMode = "light") {
                primary = MaterialTheme.colorScheme.primary
            }
        }

        composeRule.runOnIdle { assertEquals(Primary, primary) }
    }

    @Test
    fun `AdaptiveTutorTheme uses system mode and legacy alias`() {
        RuntimeEnvironment.setQualifiers("+night")
        var adaptivePrimary by mutableStateOf(Primary)
        var aliasPrimary by mutableStateOf(Primary)

        composeRule.setContent {
            AdaptiveTutorTheme {
                adaptivePrimary = MaterialTheme.colorScheme.primary
            }
            AdaptiveTutorMobileTheme {
                aliasPrimary = MaterialTheme.colorScheme.primary
            }
        }

        composeRule.runOnIdle {
            assertEquals(PrimaryDark, adaptivePrimary)
            assertEquals(PrimaryDark, aliasPrimary)
        }
    }

    @Test
    fun `AdaptiveTutorTheme falls back to system for unknown mode`() {
        RuntimeEnvironment.setQualifiers("")
        var primary by mutableStateOf(PrimaryDark)

        composeRule.setContent {
            AdaptiveTutorTheme(themeMode = "unexpected") {
                primary = MaterialTheme.colorScheme.primary
            }
        }

        composeRule.runOnIdle { assertEquals(Primary, primary) }
    }
}
