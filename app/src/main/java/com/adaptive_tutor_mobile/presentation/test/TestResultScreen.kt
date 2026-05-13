package com.adaptive_tutor_mobile.presentation.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestResultScreen(
    attemptId: String,
    viewModel: TestResultViewModel = hiltViewModel(),
    onBackToLesson: () -> Unit,
    onRetryTest: () -> Unit
) {
    // Folosim "by" pentru a extrage direct valoarea din State
    val result by viewModel.result.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Declanșăm încărcarea datelor la prima afișare a ecranului
    LaunchedEffect(attemptId) {
        viewModel.loadResult(attemptId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Rezultat Test") }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                result?.let { attemptResult ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Scor: ${attemptResult.scorePercent}%",
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Text(
                            text = if (attemptResult.passed) "Ai promovat!" else "Nu ai promovat.",
                            color = if (attemptResult.passed) Color(0xFF4CAF50) else Color.Red,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(attemptResult.questions) { qr ->
                                val isCorrect = qr.correctOptionIds.sorted() == qr.userOptionIds.sorted()

                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = qr.question.content, style = MaterialTheme.typography.bodyLarge)
                                        Text(
                                            text = "Răspunsul tău: ${qr.userOptionIds}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isCorrect) Color.DarkGray else Color.Red
                                        )
                                        if (!isCorrect) {
                                            Text(
                                                text = "Corect era: ${qr.correctOptionIds}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF2E7D32)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OutlinedButton(onClick = onBackToLesson) {
                                Text("Înapoi la curs")
                            }
                            Button(onClick = onRetryTest) {
                                Text("Reîncearcă")
                            }
                        }
                    }
                }
            }
        }
    }
}