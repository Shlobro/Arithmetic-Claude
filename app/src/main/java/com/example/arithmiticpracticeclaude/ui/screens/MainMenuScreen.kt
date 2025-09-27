package com.example.arithmiticpracticeclaude.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arithmiticpracticeclaude.data.GameRepository
import com.example.arithmiticpracticeclaude.data.League
import com.example.arithmiticpracticeclaude.ui.GameViewModel
import com.example.arithmiticpracticeclaude.ui.GameViewModelFactory
import com.example.arithmiticpracticeclaude.ui.HapticManager
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    onStartSession: () -> Unit,
    onViewStatistics: () -> Unit,
    onViewAchievements: (() -> Unit)? = null,
    onViewSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { GameRepository(context) }
    val hapticManager = remember { HapticManager(context) }
    val viewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(repository)
    )

    val gameState by viewModel.gameState.collectAsState()
    val currentLeague = League.getLeagueForScore(gameState.totalScore)

    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationStarted = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // App Title
        AnimatedVisibility(
            visible = animationStarted,
            enter = fadeIn(tween(1000)) + slideInVertically(tween(1000))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Arithmetic",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Master",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // League and Score Card
        AnimatedVisibility(
            visible = animationStarted,
            enter = fadeIn(tween(1200, delayMillis = 300)) + scaleIn(tween(1200, delayMillis = 300))
        ) {
            LeagueScoreCard(
                league = currentLeague,
                totalScore = gameState.totalScore,
                currentLevel = gameState.currentLevel,
                bestStreak = gameState.bestStreak
            )
        }

        // Progress to Next League
        if (currentLeague.name != "Eternal") {
            val nextLeague = League.leagues.find { it.minScore > currentLeague.minScore }
            if (nextLeague != null) {
                AnimatedVisibility(
                    visible = animationStarted,
                    enter = fadeIn(tween(1000, delayMillis = 600))
                ) {
                    ProgressToNextLeague(
                        currentScore = gameState.totalScore,
                        currentLeague = currentLeague,
                        nextLeague = nextLeague
                    )
                }
            }
        }

        // Action Buttons
        AnimatedVisibility(
            visible = animationStarted,
            enter = fadeIn(tween(1000, delayMillis = 900)) + slideInVertically(tween(1000, delayMillis = 900))
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Start Session Button
                Button(
                    onClick = {
                        hapticManager.buttonPressFeedback()
                        onStartSession()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Start Session",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Statistics Button
                OutlinedButton(
                    onClick = {
                        hapticManager.buttonPressFeedback()
                        onViewStatistics()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "View Statistics",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                // Achievements Button
                if (onViewAchievements != null) {
                    OutlinedButton(
                        onClick = {
                            hapticManager.buttonPressFeedback()
                            onViewAchievements()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Achievements",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                // Settings Button
                if (onViewSettings != null) {
                    OutlinedButton(
                        onClick = {
                            hapticManager.buttonPressFeedback()
                            onViewSettings()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Settings",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }

        // Session Info
        AnimatedVisibility(
            visible = animationStarted,
            enter = fadeIn(tween(800, delayMillis = 1200))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Each session contains 10 questions\ntailored to your skill level",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun LeagueScoreCard(
    league: League,
    totalScore: Long,
    currentLevel: Int,
    bestStreak: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(league.color).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // League Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = league.icon,
                    fontSize = 32.sp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = league.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(league.color)
                    )
                    Text(
                        text = "League",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.Star,
                    value = totalScore.toString(),
                    label = "Score",
                    color = Color(0xFFFFD700)
                )

                StatItem(
                    icon = Icons.Default.Add,
                    value = currentLevel.toString(),
                    label = "Level",
                    color = MaterialTheme.colorScheme.primary
                )

                StatItem(
                    icon = Icons.Default.Star,
                    value = bestStreak.toString(),
                    label = "Best Streak",
                    color = Color(0xFFFF5722)
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProgressToNextLeague(
    currentScore: Long,
    currentLeague: League,
    nextLeague: League
) {
    val progress = ((currentScore - currentLeague.minScore).toFloat() /
                   (nextLeague.minScore - currentLeague.minScore)).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Next: ${nextLeague.icon} ${nextLeague.name}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${nextLeague.minScore - currentScore} points to go",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(nextLeague.color),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}