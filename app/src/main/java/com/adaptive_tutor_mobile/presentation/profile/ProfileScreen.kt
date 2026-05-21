package com.adaptive_tutor_mobile.presentation.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adaptive_tutor_mobile.ui.components.AppTopBar
import com.adaptive_tutor_mobile.ui.components.ErrorCard
import com.adaptive_tutor_mobile.ui.components.LoadingShimmerList

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    val profile = uiState.profile

    // Câmpuri editabile — se resetează când vin datele din API
    var firstName by rememberSaveable(profile) { mutableStateOf(profile?.firstName ?: "") }
    var lastName  by rememberSaveable(profile) { mutableStateOf(profile?.lastName  ?: "") }

    // Câmpuri parolă
    var currentPassword    by rememberSaveable { mutableStateOf("") }
    var newPassword        by rememberSaveable { mutableStateOf("") }
    var newPasswordConfirm by rememberSaveable { mutableStateOf("") }
    var currentVisible     by rememberSaveable { mutableStateOf(false) }
    var newVisible         by rememberSaveable { mutableStateOf(false) }
    var confirmVisible     by rememberSaveable { mutableStateOf(false) }

    val passwordLengthError = newPassword.isNotEmpty() && newPassword.length < 8
    val passwordMatchError  = newPasswordConfirm.isNotEmpty() && newPasswordConfirm != newPassword

    Scaffold(
        topBar = { AppTopBar(title = "Profilul meu", onBack = onNavigateBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        if (uiState.isLoading) {
            LoadingShimmerList(
                itemCount = 4,
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }

        if (profile == null && uiState.errorMessage != null) {
            ErrorCard(
                message = uiState.errorMessage!!,
                onRetry = { viewModel.loadProfile() },
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Header profil ─────────────────────────────────────────────────
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${profile?.firstName?.firstOrNull() ?: ""}${profile?.lastName?.firstOrNull() ?: ""}".uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "${profile?.firstName ?: ""} ${profile?.lastName ?: ""}".trim(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (!profile?.email.isNullOrBlank()) {
                                Text(
                                    text = profile!!.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                            if (!uiState.organizationName.isNullOrBlank()) {
                                Text(
                                    text = uiState.organizationName!!,
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.55f)
                                )
                            }
                        }
                    }
                }
            }

            // ── Secțiunea 1: Date personale ──────────────────────────────────
            item {
                Text(
                    text = "Date personale",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("Prenume") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Nume") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                viewModel.updateProfile(
                                    email     = profile?.email ?: "",
                                    firstName = firstName.trim(),
                                    lastName  = lastName.trim()
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSaving
                                    && firstName.isNotBlank()
                                    && lastName.isNotBlank()
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Salvează")
                            }
                        }
                    }
                }
            }

            // ── Secțiunea 2: Securitate ───────────────────────────────────────
            item {
                Text(
                    text = "Securitate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text("Parola curentă") },
                            visualTransformation = if (currentVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { currentVisible = !currentVisible }) {
                                    Icon(
                                        imageVector = if (currentVisible) Icons.Filled.Visibility
                                        else Icons.Filled.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Parola nouă") },
                            visualTransformation = if (newVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { newVisible = !newVisible }) {
                                    Icon(
                                        imageVector = if (newVisible) Icons.Filled.Visibility
                                        else Icons.Filled.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            isError = passwordLengthError,
                            supportingText = if (passwordLengthError) {
                                { Text("Minim 8 caractere") }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = newPasswordConfirm,
                            onValueChange = { newPasswordConfirm = it },
                            label = { Text("Confirmă parola nouă") },
                            visualTransformation = if (confirmVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                    Icon(
                                        imageVector = if (confirmVisible) Icons.Filled.Visibility
                                        else Icons.Filled.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            isError = passwordMatchError,
                            supportingText = if (passwordMatchError) {
                                { Text("Parolele nu coincid") }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                viewModel.changePassword(
                                    currentPassword    = currentPassword,
                                    newPassword        = newPassword,
                                    newPasswordConfirm = newPasswordConfirm
                                )
                                currentPassword    = ""
                                newPassword        = ""
                                newPasswordConfirm = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSaving
                                    && currentPassword.isNotBlank()
                                    && newPassword.isNotBlank()
                                    && newPasswordConfirm.isNotBlank()
                                    && !passwordLengthError
                                    && !passwordMatchError
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Schimbă parola")
                            }
                        }
                    }
                }
            }

            // ── Deconectare ───────────────────────────────────────────────────
            item {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(
                        1.dp, MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Deconectare")
                }
            }
        }
    }
}