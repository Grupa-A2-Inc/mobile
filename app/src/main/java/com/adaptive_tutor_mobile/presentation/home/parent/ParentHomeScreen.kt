package com.adaptive_tutor_mobile.presentation.home.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Person
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

private val parentCards = listOf(
    ActionCard("Copiii mei",      "GET /api/v1/parents/{id}/students",        Icons.Filled.ChildCare),
    ActionCard("Progres copii",   "GET /api/v1/students/{id}/courses-progress",Icons.AutoMirrored.Filled.TrendingUp),
    ActionCard("Profilul meu",    "GET /api/v1/users/{id}",                   Icons.Filled.Person)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentHomeScreen(
    viewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()
    val firstName = currentUser?.firstName ?: "Părinte"

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
                items = parentCards,
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
