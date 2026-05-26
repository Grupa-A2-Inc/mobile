package com.adaptive_tutor_mobile.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

// ─────────────────────────────────────────────────────────────────────────────
// AppTopBar — wrapper consistent pentru toate ecranele
//
// Parametri:
//   title    — titlul ecranului curent
//   onBack   — dacă nu e null, afișează arrow înapoi și apelează callback-ul
//   actions  — slot pentru iconițe / butoane din dreapta (RowScope)
//
// Exemplu de utilizare (ecran cu back + search):
//   AppTopBar(
//       title = "Cursurile mele",
//       onBack = { navController.navigateUp() },
//       actions = {
//           IconButton(onClick = { /* caută */ }) {
//               Icon(Icons.Filled.Search, contentDescription = "Caută")
//           }
//       }
//   )
//
// Exemplu simplu (fără back, fără acțiuni):
//   AppTopBar(title = "Acasă")
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = { AppTopBarTitle(title = title) },
        navigationIcon = { if (onBack != null) AppTopBarBackButton(onBack = onBack) },
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun AppTopBarTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun AppTopBarBackButton(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Înapoi",
            tint = MaterialTheme.colorScheme.onSurface)
    }
}
