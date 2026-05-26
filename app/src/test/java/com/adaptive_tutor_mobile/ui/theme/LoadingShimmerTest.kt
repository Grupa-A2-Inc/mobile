package com.adaptive_tutor_mobile.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LoadingShimmerTest {

    @get:Rule
    val composeRule = createComposeRule()

    @After
    fun resetQualifiers() {
        RuntimeEnvironment.setQualifiers("")
    }

    @Test
    fun `shimmer components compose in light mode`() {
        composeRule.setContent {
            AdaptiveTutorTheme {
                Column {
                    ShimmerBlock(modifier = Modifier)
                    ShimmerBlock(
                        modifier = Modifier,
                        height = 20.dp,
                        cornerRadius = 6.dp,
                        brush = SolidColor(Color.Red)
                    )
                    CourseCardShimmer(modifier = Modifier)
                    HeaderShimmer(modifier = Modifier)
                    RowShimmer(modifier = Modifier)
                }
            }
        }

        composeRule.onRoot().assertIsDisplayed()
    }

    @Test
    fun `loading shimmer list composes in dark mode`() {
        RuntimeEnvironment.setQualifiers("+night")

        composeRule.setContent {
            AdaptiveTutorTheme {
                LoadingShimmerList(itemCount = 2, modifier = Modifier)
            }
        }

        composeRule.onRoot().assertIsDisplayed()
    }
}
