package com.adaptive_tutor_mobile.presentation.home.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.adaptive_tutor_mobile.presentation.auth.AuthViewModel
import kotlinx.coroutines.launch

private data class ActionCard(val title: String, val subtitle: String, val icon: ImageVector)

private val teacherCards = listOf(
    ActionCard("Cursurile mele",  "GET /api/v1/courses/my-courses",             Icons.AutoMirrored.Filled.MenuBook),
    ActionCard("Curs nou",        "POST /api/v1/courses",                        Icons.Filled.AddCircle),
    ActionCard("Alerte active",   "GET /api/v1/professors/me/alerts",            Icons.Filled.NotificationsActive),
    ActionCard("Generare AI",     "POST /api/v1/lessons/{id}/ai/generate-test",  Icons.Filled.AutoAwesome)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherHomeScreen(
    viewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()
    val firstName = currentUser?.firstName ?: "Profesor"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bună, $firstName") },
                actions = {
                    IconButton(onClick = { viewModel.logout(); onLogout() }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Deconectare")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 180.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = teacherCards,
                key = { card -> card.title }
            ) { card ->
                ElevatedCard(
                    onClick = { scope.launch { snackbarHostState.showSnackbar("Coming soon") } }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(card.icon, contentDescription = card.title)
                        Text(card.title, style = MaterialTheme.typography.titleSmall)
                        Text(card.subtitle, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
