package com.adaptive_tutor_mobile.presentation.test

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adaptive_tutor_mobile.domain.model.Question
import com.adaptive_tutor_mobile.domain.model.QuestionType

@Composable
fun QuestionCard(
    question: Question,
    selectedOptionIds: List<Int>,
    onAnswerSelected: (List<Int>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = question.content, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            when (question.type) {
                QuestionType.SINGLE_CHOICE, QuestionType.TRUE_FALSE -> {
                    question.options.forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedOptionIds.contains(option.id),
                                onClick = { onAnswerSelected(listOf(option.id)) }
                            )
                            Text(text = option.text)
                        }
                    }
                }
                QuestionType.MULTI_CHOICE -> {
                    question.options.forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = selectedOptionIds.contains(option.id),
                                onCheckedChange = { isChecked ->
                                    val newSelection = selectedOptionIds.toMutableList()
                                    if (isChecked) newSelection.add(option.id)
                                    else newSelection.remove(option.id)
                                    onAnswerSelected(newSelection)
                                }
                            )
                            Text(text = option.text)
                        }
                    }
                }
            }
        }
    }
}