package com.adaptive_tutor_mobile.presentation.adaptive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adaptive_tutor_mobile.domain.model.ExerciseResult
import com.adaptive_tutor_mobile.presentation.components.AdaptiveTopBar
import com.adaptive_tutor_mobile.presentation.components.EmptyScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment

@Composable
fun AdaptiveResultScreen(
    onBackToHome: () -> Unit,
    viewModel: AdaptiveViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val result = state.result

    Scaffold(
        topBar = {
            AdaptiveTopBar(title = "Rezultat sesiune", onBack = onBackToHome)
        }
    ) { innerPadding ->
        if (result == null) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                EmptyScreen(
                    message = "Nu există încă un rezultat pentru sesiunea adaptivă.",
                    icon = Icons.Filled.Assessment
                )
                Button(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text("Înapoi la home")
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Scor total",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${result.totalScore}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            items(result.results) { exerciseResult ->
                ResultCard(exerciseResult)
            }

            item {
                Button(
                    onClick = onBackToHome,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Înapoi la home")
                }
            }
        }
    }
}

@Composable
private fun ResultCard(result: ExerciseResult) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (result.correct) "Corect" else "Greșit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (result.correct) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            Text(text = "Exercițiu: ${result.exerciseId}")
            Text(text = "Scor: ${result.score}")
            Text(text = "Răspunsul tău: ${result.givenAnswers.joinToString(", ")}")
            Text(text = "Răspuns corect: ${result.correctAnswers.joinToString(", ")}")
        }
    }
}