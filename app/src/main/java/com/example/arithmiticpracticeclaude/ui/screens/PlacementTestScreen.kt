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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arithmiticpracticeclaude.data.*
import com.example.arithmiticpracticeclaude.ui.GameViewModel
import com.example.arithmiticpracticeclaude.ui.GameViewModelFactory
import com.example.arithmiticpracticeclaude.ui.PlacementViewModel
import com.example.arithmiticpracticeclaude.ui.PlacementViewModelFactory
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PlacementTestScreen(
    onPlacementComplete: (placedLeague: String, finalScore: Long) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val repository = remember { GameRepository(context) }
    val viewModel: PlacementViewModel = viewModel(
        factory = PlacementViewModelFactory(repository)
    )

    val placementTest by viewModel.placementTest.collectAsState()
    val currentLevel by viewModel.currentLevel.collectAsState()
    val currentQuestion by viewModel.currentQuestion.collectAsState()
    val showResult by viewModel.showResult.collectAsState()
    val lastResult by viewModel.lastResult.collectAsState()
    val isComplete by viewModel.isComplete.collectAsState()
    val phase by viewModel.currentPhase.collectAsState()

    var userAnswer by remember { mutableStateOf("") }
    var timeLeft by remember { mutableStateOf(30) }

    // Handle placement completion
    LaunchedEffect(isComplete) {
        if (isComplete && placementTest != null) {
            onPlacementComplete(placementTest!!.placedInLeague, placementTest!!.finalScore)
        }
    }

    // Reset timer when new question appears
    LaunchedEffect(currentQuestion) {
        timeLeft = currentLevel?.timeLimit ?: 30
    }

    // Timer Effect
    LaunchedEffect(showResult, timeLeft, currentQuestion) {
        if (!showResult && timeLeft > 0 && currentQuestion != null) {
            delay(1000)
            timeLeft--
        } else if (timeLeft == 0 && !showResult && currentQuestion != null) {
            // Time's up - submit incorrect answer
            viewModel.submitAnswer(-1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (phase) {
            PlacementPhase.INTRO -> {
                PlacementIntroCard(
                    onStart = { viewModel.startPlacementTest() }
                )
            }
            PlacementPhase.TESTING -> {
                // Placement Header
                PlacementHeaderCard(
                    placementTest = placementTest,
                    currentLevel = currentLevel
                )

                // Timer Display
                TimerCard(
                    timeLeft = timeLeft,
                    isVisible = !showResult,
                    maxTime = currentLevel?.timeLimit ?: 30
                )

                // Question Card
                currentQuestion?.let { question ->
                    PlacementQuestionCard(
                        question = question,
                        levelName = currentLevel?.name ?: "",
                        level = currentLevel?.level ?: 1
                    )
                }

                // Answer Input
                PlacementAnswerInput(
                    value = userAnswer,
                    onValueChange = { userAnswer = it },
                    enabled = !showResult,
                    onSubmit = {
                        val answer = userAnswer.toIntOrNull()
                        if (answer != null) {
                            viewModel.submitAnswer(answer)
                        }
                    }
                )

                // Submit Button
                PlacementSubmitButton(
                    enabled = userAnswer.isNotEmpty() && !showResult,
                    onClick = {
                        val answer = userAnswer.toIntOrNull()
                        if (answer != null) {
                            viewModel.submitAnswer(answer)
                        }
                    }
                )

                // Result Display
                lastResult?.let { result ->
                    PlacementResultCard(
                        result = result,
                        visible = showResult,
                        onNext = {
                            userAnswer = ""
                            viewModel.nextQuestion()
                        }
                    )
                }
            }
            PlacementPhase.COMPLETE -> {
                placementTest?.let { test ->
                    PlacementCompletionCard(
                        placementTest = test,
                        onContinue = {
                            onPlacementComplete(test.placedInLeague, test.finalScore)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PlacementIntroCard(
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Welcome to Arithmetic League!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "You'll take a one-time placement test to determine your starting league. Questions will get progressively harder until you reach your skill level.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Text(
                text = "⚠️ This test can only be taken once!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Start Placement Test",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PlacementHeaderCard(
    placementTest: PlacementTest?,
    currentLevel: PlacementLevel?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Placement Test",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Level ${currentLevel?.level ?: 1}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = currentLevel?.name ?: "Starting",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${placementTest?.questionsAnswered ?: 0}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Questions",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${placementTest?.lastCorrectLevel ?: 0}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                    Text(
                        text = "Best Level",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            currentLevel?.let { level ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = level.operations.joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlacementQuestionCard(
    question: com.example.arithmiticpracticeclaude.ui.ArithmeticQuestion,
    levelName: String,
    level: Int
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
                .height(220.dp),
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
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = levelName,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
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
fun PlacementAnswerInput(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
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
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun PlacementSubmitButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Submit Answer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PlacementResultCard(
    result: QuestionResult,
    visible: Boolean,
    onNext: () -> Unit
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
                    text = if (result.isCorrect) "Correct! Moving up..." else "Incorrect. Final placement determined.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                if (!result.isCorrect) {
                    Text(
                        text = "Correct answer: ${result.correctAnswer}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = backgroundColor
                    )
                ) {
                    Text(
                        if (result.isCorrect) "Continue" else "View Results",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PlacementCompletionCard(
    placementTest: PlacementTest,
    onContinue: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Placement Complete!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "You've been placed in:",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = placementTest.placedInLeague,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Highest level reached: ${placementTest.lastCorrectLevel}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Total questions: ${placementTest.questionsAnswered}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Start Playing!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

enum class PlacementPhase {
    INTRO, TESTING, COMPLETE
}