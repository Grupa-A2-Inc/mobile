package com.adaptive_tutor_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.adaptive_tutor_mobile.di.SessionStore
import com.adaptive_tutor_mobile.di.SessionStoreEntryPoint
import com.adaptive_tutor_mobile.presentation.navigation.AppNavGraph
import com.adaptive_tutor_mobile.presentation.navigation.Screen
import com.adaptive_tutor_mobile.ui.theme.AdaptiveTutorTheme
import com.adaptive_tutor_mobile.ui.theme.ThemeViewModel
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val sessionStore: SessionStore by lazy(LazyThreadSafetyMode.NONE) {
        EntryPointAccessors.fromApplication(
            applicationContext,
            SessionStoreEntryPoint::class.java
        ).sessionStore()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()

            AdaptiveTutorTheme(themeMode = themeMode) {
                AppNavGraph(startDestination = Screen.Splash.route, sessionStore = sessionStore)
            }
        }
    }
}
