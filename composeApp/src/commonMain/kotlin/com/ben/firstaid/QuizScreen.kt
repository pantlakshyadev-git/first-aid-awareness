package com.ben.firstaid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun QuizScreen(state: AppState) {
    val session = state.quiz
    when {
        session == null -> QuizSetup(state)
        session.finished -> QuizResult(state, session)
        else -> QuizQuestionPage(state, session)
    }
}

@Composable
private fun QuizSetup(state: AppState) {
    var menuOpen by remember { mutableStateOf(false) }
    val categories = listOf("All") + state.data.quizQuestions.map { it.category }.distinct().sorted()
    val available = state.data.quizQuestions.count { state.quizCategory == "All" || it.category == state.quizCategory }
    Page {
        PageTitle(tr("Knowledge Quiz"), tr("Practise with questions from the bundled knowledge base. You get feedback after every answer."))
        AppCard {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(tr("Category"), fontWeight = FontWeight.Bold, color = AppBlue)
                Box {
                    OutlinedButton(onClick = { menuOpen = true }, shape = RoundedCornerShape(10.dp)) {
                        Text(tr(state.quizCategory), fontWeight = FontWeight.Medium)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(tr(category)) },
                                onClick = {
                                    state.quizCategory = category
                                    menuOpen = false
                                },
                            )
                        }
                    }
                }
                Text(tr("Number of questions"), fontWeight = FontWeight.Bold, color = AppBlue)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 20).forEach { n ->
                        FilterChip(
                            selected = state.quizCount == n,
                            onClick = { state.quizCount = n },
                            label = { Text("$n") },
                            shape = RoundedCornerShape(10.dp),
                        )
                    }
                }
                Text(
                    tr("{0} questions available in this category.", available),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Button(
            onClick = { state.startQuiz() },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text(tr("Start quiz"), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuizQuestionPage(state: AppState, session: QuizSession) {
    key(session.index) {
        val question = session.current
        val total = session.questions.size
        Page {
            PageTitle(
                tr("Knowledge Quiz"),
                tr("Question {0} of {1} · {2}", session.index + 1, total, tr(state.quizCategory)),
            )
            LinearProgressIndicator(
                progress = { (session.index + if (session.answered) 1 else 0).toFloat() / total },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = AppBlue,
                trackColor = SoftBlue,
            )
            AppCard {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        question.question,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    question.options.forEach { option ->
                        RadioRow(
                            label = option,
                            selected = session.selected == option,
                            enabled = !session.answered,
                            onClick = { session.choose(option) },
                        )
                    }
                    if (session.answered) {
                        val correct = session.selected == question.answer
                        val containerColor = if (correct) Color(0xFFE6F4EA) else Color(0xFFFCE8E6)
                        val textColor = if (correct) Color(0xFF137333) else Color(0xFFC5221F)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = containerColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    if (correct) tr("Correct!") else tr("Not quite. Correct answer: {0}", question.answer),
                                    color = textColor,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (question.explanation.isNotBlank()) {
                                    Text(
                                        question.explanation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OutlinedButton(onClick = { state.endQuiz() }, shape = RoundedCornerShape(10.dp)) {
                    Text(tr("Exit quiz"))
                }
                Button(
                    onClick = { session.next() },
                    enabled = session.answered,
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(if (session.isLast) tr("Finish") else tr("Next question"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun QuizResult(state: AppState, session: QuizSession) {
    val total = session.questions.size
    Page {
        PageTitle(tr("Quiz Complete"), tr("Use the result as a learning checkpoint, not as a certification."))
        AppCard(containerColor = SoftBlue) {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "${session.score} / $total",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppBlue,
                )
                Text(
                    tr("Score: {0}% · {1}", percent(session.score, total), tr(state.quizCategory)),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(tr(quizMessage(session.score, total)), style = MaterialTheme.typography.bodyMedium)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { state.startQuiz() }, shape = RoundedCornerShape(10.dp)) {
                Text(tr("Try again"), fontWeight = FontWeight.Bold)
            }
            OutlinedButton(onClick = { state.endQuiz() }, shape = RoundedCornerShape(10.dp)) {
                Text(tr("Change settings"))
            }
        }
    }
}
