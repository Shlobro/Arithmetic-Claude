package com.example.arithmiticpracticeclaude.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arithmiticpracticeclaude.data.GameRepository
import com.example.arithmiticpracticeclaude.ui.GameViewModel
import com.example.arithmiticpracticeclaude.ui.GameViewModelFactory
import com.example.arithmiticpracticeclaude.data.League
import kotlinx.coroutines.delay
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SimpleGameScreen(
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
    var timeLeft by remember { mutableStateOf(30) }
    var questionStartTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Start session on first load
    LaunchedEffect(Unit) {
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
    LaunchedEffect(currentQuestion, gameState.totalScore) {
        timeLeft = League.getTimerForScore(gameState.totalScore)
        questionStartTime = System.currentTimeMillis()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Enhanced Header with Progress
        EnhancedHeaderCard(
            gameState = gameState,
            currentSession = currentSession
        )

        // Enhanced Timer Display
        TimerCard(
            timeLeft = timeLeft,
            isVisible = !showResult,
            maxTime = League.getTimerForScore(gameState.totalScore)
        )

        // Enhanced Question Card
        currentQuestion?.let { question ->
            EnhancedQuestionCard(
                question = question,
                difficulty = gameState.currentLevel
            )
        }

        // Enhanced Answer Input with Submit Button
        EnhancedAnswerInput(
            value = userAnswer,
            onValueChange = { userAnswer = it },
            enabled = !showResult,
            canSubmit = userAnswer.isNotEmpty() && !showResult,
            onSubmit = {
                val answer = userAnswer.toIntOrNull()
                if (answer != null) {
                    viewModel.submitAnswer(answer)
                }
            }
        )

        // Enhanced Result Display
        lastResult?.let { result ->
            EnhancedResultCard(
                result = result,
                visible = showResult,
                onNext = {
                    viewModel.nextQuestion()
                    userAnswer = ""
                },
                onEndSession = {
                    viewModel.endSession()
                }
            )
        }


        // Enhanced Session Statistics
        currentSession?.let { session ->
            if (session.questionsAnswered > 0) {
                SessionStatsCard(
                    session = session,
                    gameState = gameState
                )
            }
        }
    }

    // Timer Effect
    LaunchedEffect(showResult, timeLeft) {
        if (!showResult && timeLeft > 0) {
            delay(1000)
            timeLeft--
        } else if (timeLeft == 0 && !showResult) {
            // Time's up - submit incorrect answer
            viewModel.submitAnswer(-1) // Invalid answer to trigger incorrect
        }
    }
}

@Composable
fun EnhancedHeaderCard(
    gameState: com.example.arithmiticpracticeclaude.data.GameState,
    currentSession: com.example.arithmiticpracticeclaude.data.GameSession?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Arithmetic Practice",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // League and score display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val currentLeague = League.getLeagueForScore(gameState.totalScore)
                    Text(
                        text = "${currentLeague.icon} ${currentLeague.name}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(currentLeague.color)
                    )
                    Text(
                        text = currentLeague.targetAge,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${gameState.totalScore}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Total Score",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${gameState.currentStreak}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                    Text(
                        text = "Streak",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Session progress bar
            currentSession?.let { session ->
                if (session.questionsAnswered > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val progress = session.accuracy / 100f
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (progress >= 0.7f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Session: ${session.questionsAnswered} questions, ${session.accuracy.roundToInt()}% accuracy",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun TimerCard(
    timeLeft: Int,
    isVisible: Boolean,
    maxTime: Int
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        val progress = timeLeft.toFloat() / maxTime
        val color = when {
            timeLeft <= 5 -> Color(0xFFF44336)
            timeLeft <= 10 -> Color(0xFFFF9800)
            else -> MaterialTheme.colorScheme.primary
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = color
                    )
                    Text(
                        text = "${timeLeft}s",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = color
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EnhancedQuestionCard(
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
                .height(200.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Difficulty indicator
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Level $difficulty",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = questionText,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedAnswerInput(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    canSubmit: Boolean,
    onSubmit: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Your Answer") },
        modifier = Modifier.fillMaxWidth(),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 28.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        ),
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            IconButton(
                onClick = onSubmit,
                enabled = canSubmit
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Submit Answer",
                    tint = if (canSubmit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}


@Composable
fun EnhancedResultCard(
    result: com.example.arithmiticpracticeclaude.data.QuestionResult,
    visible: Boolean,
    onNext: () -> Unit,
    onEndSession: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it }
    ) {
        val backgroundColor = if (result.isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
        val icon = if (result.isCorrect) Icons.Default.CheckCircle else Icons.Default.Close

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
                Text(
                    text = if (result.isCorrect) "Correct! 🎉" else "Incorrect! 😞",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                if (!result.isCorrect) {
                    Text(
                        text = "Correct answer: ${result.correctAnswer}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                // Score change display
                if (result.scoreChange != 0) {
                    Text(
                        text = "${if (result.scoreChange > 0) "+" else ""}${result.scoreChange} points",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Response time
                Text(
                    text = "Response time: ${result.responseTime / 1000}s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNext,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = backgroundColor
                        )
                    ) {
                        Text("Next Question", fontWeight = FontWeight.Medium)
                    }

                    OutlinedButton(
                        onClick = onEndSession,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                    ) {
                        Text("End Session", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameModeCard(
    currentMode: String,
    onModeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Game Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank")) { mode ->
                    FilterChip(
                        onClick = { onModeChange(mode) },
                        label = { Text(mode) },
                        selected = currentMode == mode,
                        leadingIcon = if (currentMode == mode) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
fun SessionStatsCard(
    session: com.example.arithmiticpracticeclaude.data.GameSession,
    gameState: com.example.arithmiticpracticeclaude.data.GameState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Session Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${session.accuracy.roundToInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (session.accuracy >= 70) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    Text(
                        text = "Accuracy",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${session.correctAnswers}/${session.questionsAnswered}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Correct",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "+${session.scoreGained}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                    Text(
                        text = "Session",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}