package com.adaptive_tutor_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.adaptive_tutor_mobile.di.SessionStore
import com.adaptive_tutor_mobile.presentation.navigation.AppNavGraph
import com.adaptive_tutor_mobile.ui.theme.AdaptiveTutorTheme
import com.adaptive_tutor_mobile.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionStore: SessionStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()

            AdaptiveTutorTheme(themeMode = themeMode) {
                AppNavGraph(sessionStore = sessionStore)
            }
        }
    }
}