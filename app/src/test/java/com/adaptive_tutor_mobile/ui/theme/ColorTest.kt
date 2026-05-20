package com.adaptive_tutor_mobile.ui.theme

import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorTest {

    @Test
    fun `color palette matches expected values`() {
        assertEquals(0xFF4A90D9.toInt(), Primary.toArgb())
        assertEquals(0xFFFFFFFF.toInt(), OnPrimary.toArgb())
        assertEquals(0xFFD6E8F7.toInt(), PrimaryContainer.toArgb())
        assertEquals(0xFF0D2E4A.toInt(), OnPrimaryContainer.toArgb())
        assertEquals(0xFF6BAED6.toInt(), Secondary.toArgb())
        assertEquals(0xFFFFFFFF.toInt(), OnSecondary.toArgb())
        assertEquals(0xFFDDEEF8.toInt(), SecondaryContainer.toArgb())
        assertEquals(0xFF3A7ABF.toInt(), Tertiary.toArgb())
        assertEquals(0xFFF0F6FD.toInt(), BackgroundLight.toArgb())
        assertEquals(0xFFFFFFFF.toInt(), SurfaceLight.toArgb())
        assertEquals(0xFFE3F0FB.toInt(), SurfaceVariantLight.toArgb())
        assertEquals(0xFF0D1B2A.toInt(), OnBackgroundLight.toArgb())
        assertEquals(0xFF0D1B2A.toInt(), OnSurfaceLight.toArgb())
        assertEquals(0xFF7BB8E8.toInt(), PrimaryDark.toArgb())
        assertEquals(0xFF0D2E4A.toInt(), OnPrimaryDark.toArgb())
        assertEquals(0xFF1A4A7A.toInt(), PrimaryContainerDark.toArgb())
        assertEquals(0xFFD6E8F7.toInt(), OnPrimaryContainerDark.toArgb())
        assertEquals(0xFF9ECAE8.toInt(), SecondaryDark.toArgb())
        assertEquals(0xFF0D2E4A.toInt(), OnSecondaryDark.toArgb())
        assertEquals(0xFF1A3A5C.toInt(), SecondaryContainerDark.toArgb())
        assertEquals(0xFF0D1B2A.toInt(), BackgroundDark.toArgb())
        assertEquals(0xFF152336.toInt(), SurfaceDark.toArgb())
        assertEquals(0xFF1E3048.toInt(), SurfaceVariantDark.toArgb())
        assertEquals(0xFFD6E8F7.toInt(), OnBackgroundDark.toArgb())
        assertEquals(0xFFD6E8F7.toInt(), OnSurfaceDark.toArgb())
        assertEquals(0xFFE53935.toInt(), ErrorColor.toArgb())
        assertEquals(0xFFE3F0FB.toInt(), ShimmerBase.toArgb())
        assertEquals(0xFFF5FAFF.toInt(), ShimmerHighlight.toArgb())
        assertEquals(0xFF1E3048.toInt(), ShimmerBaseDark.toArgb())
        assertEquals(0xFF263D57.toInt(), ShimmerHighlightDark.toArgb())
    }
}
