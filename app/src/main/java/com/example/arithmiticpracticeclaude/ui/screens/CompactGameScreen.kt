package com.example.arithmiticpracticeclaude.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arithmiticpracticeclaude.data.GameRepository
import com.example.arithmiticpracticeclaude.data.League
import com.example.arithmiticpracticeclaude.ui.GameViewModel
import com.example.arithmiticpracticeclaude.ui.GameViewModelFactory
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CompactGameScreen(
    onSessionComplete: (gameState: com.example.arithmiticpracticeclaude.data.GameSession,
                       gameStateData: com.example.arithmiticpracticeclaude.data.GameState) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val repository = remember { GameRepository(context) }
    val viewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(repository)
    )

    val gameState by viewModel.gameState.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState()
    val currentQuestion by viewModel.currentQuestion.collectAsState()
    val showResult by viewModel.showResult.collectAsState()
    val lastResult by viewModel.lastResult.collectAsState()
    val sessionEnded by viewModel.sessionEnded.collectAsState()

    var userAnswer by remember { mutableStateOf("") }
    val timerDuration = League.getTimerForScore(gameState.totalScore)
    var timeLeft by remember { mutableStateOf(timerDuration) }

    // Start session on first load - use a more stable key
    LaunchedEffect(currentSession == null) {
        if (currentSession == null) {
            viewModel.startNewSession()
        }
    }

    // Handle session completion
    LaunchedEffect(sessionEnded) {
        if (sessionEnded && currentSession != null) {
            onSessionComplete(currentSession!!, gameState)
        }
    }

    // Reset timer when new question appears
    LaunchedEffect(currentQuestion, timerDuration) {
        timeLeft = timerDuration
    }

    // Timer Effect
    LaunchedEffect(showResult, timeLeft) {
        if (!showResult && timeLeft > 0) {
            delay(1000)
            timeLeft--
        } else if (timeLeft == 0 && !showResult) {
            viewModel.submitAnswer(-1) // Invalid answer to trigger incorrect
        }
    }

    val currentLeague = League.getLeagueForScore(gameState.totalScore)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        Color(currentLeague.color).copy(alpha = 0.05f)
                    )
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Compact Header
        CompactHeaderCard(
            league = currentLeague,
            totalScore = gameState.totalScore,
            currentLevel = gameState.currentLevel,
            questionsAnswered = currentSession?.questionsAnswered ?: 0,
            timeLeft = timeLeft,
            gameMode = gameState.gameMode
        )

        // Question Card
        currentQuestion?.let { question ->
            CompactQuestionCard(
                question = question,
                difficulty = gameState.currentLevel
            )
        }

        // Answer Input and Submit
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = userAnswer,
                onValueChange = { userAnswer = it },
                label = { Text("Your Answer") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                ),
                singleLine = true,
                enabled = !showResult,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    val answer = userAnswer.toIntOrNull()
                    if (answer != null) {
                        viewModel.submitAnswer(answer)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = userAnswer.isNotEmpty() && !showResult,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Submit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Result Display
        lastResult?.let { result ->
            AnimatedVisibility(
                visible = showResult,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                CompactResultCard(
                    result = result,
                    onNext = {
                        // Only continue if session isn't complete
                        val questionsCount = currentSession?.questionsAnswered ?: 0
                        if (questionsCount < 10 && !sessionEnded) {
                            viewModel.nextQuestion()
                            userAnswer = ""
                        }
                        // If session is complete, the sessionEnded effect will handle navigation
                    }
                )
            }
        }
    }
}

@Composable
fun CompactHeaderCard(
    league: League,
    totalScore: Long,
    currentLevel: Int,
    questionsAnswered: Int,
    timeLeft: Int,
    gameMode: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(league.color).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top row: League and Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = league.icon, fontSize = 20.sp)
                    Text(
                        text = league.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(league.color)
                    )
                }
                Text(
                    text = "$totalScore pts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Bottom row: Progress and Timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Q${questionsAnswered + 1}/10",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = gameMode,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Lv.$currentLevel",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Timer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = when {
                            timeLeft <= 5 -> Color(0xFFF44336)
                            timeLeft <= 10 -> Color(0xFFFF9800)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = "${timeLeft}s",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            timeLeft <= 5 -> Color(0xFFF44336)
                            timeLeft <= 10 -> Color(0xFFFF9800)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CompactQuestionCard(
    question: com.example.arithmiticpracticeclaude.ui.ArithmeticQuestion,
    difficulty: Int
) {
    AnimatedContent(
        targetState = question.text,
        transitionSpec = {
            slideInHorizontally { it } + fadeIn() with
            slideOutHorizontally { -it } + fadeOut()
        }
    ) { questionText ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = questionText,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun CompactResultCard(
    result: com.example.arithmiticpracticeclaude.data.QuestionResult,
    onNext: () -> Unit
) {
    val backgroundColor = if (result.isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
    val icon = if (result.isCorrect) Icons.Default.CheckCircle else Icons.Default.Close

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
                Text(
                    text = if (result.isCorrect) "Correct!" else "Incorrect",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!result.isCorrect) {
                    Text(
                        text = "Answer: ${result.correctAnswer}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                if (result.scoreChange != 0) {
                    Text(
                        text = "${if (result.scoreChange > 0) "+" else ""}${result.scoreChange} pts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue", fontWeight = FontWeight.Medium)
            }
        }
    }
}