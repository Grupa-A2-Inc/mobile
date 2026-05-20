package com.adaptive_tutor_mobile.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.adaptive_tutor_mobile.data.remote.dto.RegisterRequest
import com.adaptive_tutor_mobile.domain.model.auth.User
import androidx.compose.material3.ExperimentalMaterial3Api

private val emailRegexRegister = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private val organizationTypes = listOf("SCHOOL", "UNIVERSITY", "COMPANY", "OTHER")

// ── Form State Data Class ─────────────────────────────────────────────────────
private data class RegisterFormState(
    // Personal data
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val confirmVisible: Boolean = false,
    
    // Organization data
    val organizationName: String = "",
    val organizationType: String = organizationTypes[0],
    val orgTypeExpanded: Boolean = false,
    val country: String = "",
    val city: String = "",
    val address: String = "",
    val phoneNumber: String = "",
    
    // Validation
    val errors: Map<String, String> = emptyMap()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: (User) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var formState by remember { mutableStateOf(RegisterFormState()) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onRegisterSuccess((uiState as AuthUiState.Success).user)
            viewModel.resetState()
        }
    }

    Scaffold { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Înregistrare", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Date personale", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                OutlinedTextField(
                    value = formState.firstName,
                    onValueChange = { formState = formState.copy(firstName = it) },
                    label = { Text("Prenume") },
                    isError = formState.errors["firstName"] != null,
                    supportingText = formState.errors["firstName"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = formState.lastName,
                    onValueChange = { formState = formState.copy(lastName = it) },
                    label = { Text("Nume") },
                    isError = formState.errors["lastName"] != null,
                    supportingText = formState.errors["lastName"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = formState.email,
                    onValueChange = { formState = formState.copy(email = it) },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = formState.errors["email"] != null,
                    supportingText = formState.errors["email"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = formState.password,
                    onValueChange = { formState = formState.copy(password = it) },
                    label = { Text("Parolă") },
                    visualTransformation = if (formState.passwordVisible) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { formState = formState.copy(passwordVisible = !formState.passwordVisible) }) {
                            Icon(
                                imageVector = if (formState.passwordVisible) Icons.Filled.Visibility
                                              else Icons.Filled.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    isError = formState.errors["password"] != null,
                    supportingText = formState.errors["password"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = formState.confirmPassword,
                    onValueChange = { formState = formState.copy(confirmPassword = it) },
                    label = { Text("Confirmă parola") },
                    visualTransformation = if (formState.confirmVisible) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { formState = formState.copy(confirmVisible = !formState.confirmVisible) }) {
                            Icon(
                                imageVector = if (formState.confirmVisible) Icons.Filled.Visibility
                                              else Icons.Filled.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    isError = formState.errors["confirmPassword"] != null,
                    supportingText = formState.errors["confirmPassword"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Organizație", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                OutlinedTextField(
                    value = formState.organizationName,
                    onValueChange = { formState = formState.copy(organizationName = it) },
                    label = { Text("Numele organizației") },
                    isError = formState.errors["organizationName"] != null,
                    supportingText = formState.errors["organizationName"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                ExposedDropdownMenuBox(
                    expanded = formState.orgTypeExpanded,
                    onExpandedChange = { formState = formState.copy(orgTypeExpanded = it) }
                ) {
                    OutlinedTextField(
                        value = formState.organizationType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipul organizației") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formState.orgTypeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = formState.orgTypeExpanded,
                        onDismissRequest = { formState = formState.copy(orgTypeExpanded = false) }
                    ) {
                        organizationTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = { formState = formState.copy(organizationType = type, orgTypeExpanded = false) }
                            )
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = formState.country,
                    onValueChange = { formState = formState.copy(country = it) },
                    label = { Text("Țară") },
                    isError = formState.errors["country"] != null,
                    supportingText = formState.errors["country"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = formState.city,
                    onValueChange = { formState = formState.copy(city = it) },
                    label = { Text("Oraș") },
                    isError = formState.errors["city"] != null,
                    supportingText = formState.errors["city"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = formState.address,
                    onValueChange = { formState = formState.copy(address = it) },
                    label = { Text("Adresă (opțional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = formState.phoneNumber,
                    onValueChange = { formState = formState.copy(phoneNumber = it) },
                    label = { Text("Telefon (opțional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            if (uiState is AuthUiState.Error) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = (uiState as AuthUiState.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val errs = mutableMapOf<String, String>()
                        if (formState.firstName.isBlank()) errs["firstName"] = "Câmp obligatoriu"
                        if (formState.lastName.isBlank())  errs["lastName"]  = "Câmp obligatoriu"
                        if (!emailRegexRegister.matches(formState.email)) errs["email"] = "Email invalid"
                        if (formState.password.length < 8) errs["password"] = "Minim 8 caractere"
                        if (formState.confirmPassword != formState.password) errs["confirmPassword"] = "Parolele nu coincid"
                        if (formState.organizationName.isBlank()) errs["organizationName"] = "Câmp obligatoriu"
                        if (formState.country.isBlank()) errs["country"] = "Câmp obligatoriu"
                        if (formState.city.isBlank()) errs["city"] = "Câmp obligatoriu"
                        
                        formState = formState.copy(errors = errs)
                        
                        if (errs.isEmpty()) {
                            viewModel.register(
                                RegisterRequest(
                                    firstName = formState.firstName.trim(),
                                    lastName  = formState.lastName.trim(),
                                    email     = formState.email.trim(),
                                    password  = formState.password,
                                    confirmPassword = formState.confirmPassword,
                                    organizationName = formState.organizationName.trim(),
                                    country  = formState.country.trim(),
                                    city     = formState.city.trim(),
                                    organizationType = formState.organizationType,
                                    address     = formState.address.trim().takeIf { it.isNotEmpty() },
                                    phoneNumber = formState.phoneNumber.trim().takeIf { it.isNotEmpty() }
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = uiState !is AuthUiState.Loading
                ) {
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Creează cont")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ai deja cont? Autentifică-te")
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
