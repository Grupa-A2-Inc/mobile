package com.adaptive_tutor_mobile

import android.app.Application
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveTutorAppTest {

    @Test
    fun `AdaptiveTutorApp extends Application`() {
        assertTrue(AdaptiveTutorApp() is Application)
    }
}
