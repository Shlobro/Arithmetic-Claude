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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arithmiticpracticeclaude.data.GameSession
import com.example.arithmiticpracticeclaude.data.GameState
import com.example.arithmiticpracticeclaude.data.League
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactSessionSummaryScreen(
    session: GameSession,
    gameState: GameState,
    onReturnToMenu: () -> Unit,
    onViewStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animationStarted by remember { mutableStateOf(false) }
    val currentLeague = League.getLeagueForScore(gameState.totalScore)
    val levelChanged = session.endingLevel != session.startingLevel

    LaunchedEffect(Unit) {
        animationStarted = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(currentLeague.color).copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Session Complete Header
        AnimatedVisibility(
            visible = animationStarted,
            enter = fadeIn(tween(800)) + slideInVertically(tween(800))
        ) {
            SessionCompleteHeader(
                session = session,
                currentLeague = currentLeague,
                levelChanged = levelChanged
            )
        }

        // Performance Summary
        AnimatedVisibility(
            visible = animationStarted,
            enter = fadeIn(tween(1000, delayMillis = 200)) + scaleIn(tween(1000, delayMillis = 200))
        ) {
            PerformanceSummaryCard(session = session)
        }

        // League Status
        AnimatedVisibility(
            visible = animationStarted,
            enter = fadeIn(tween(1000, delayMillis = 400))
        ) {
            LeagueStatusCard(
                currentLeague = currentLeague,
                totalScore = gameState.totalScore,
                gameState = gameState
            )
        }

        // Action Buttons
        AnimatedVisibility(
            visible = animationStarted,
            enter = fadeIn(tween(800, delayMillis = 600)) + slideInVertically(tween(800, delayMillis = 600))
        ) {
            ActionButtonsRow(
                onReturnToMenu = onReturnToMenu,
                onViewStats = onViewStats
            )
        }
    }
}

@Composable
fun SessionCompleteHeader(
    session: GameSession,
    currentLeague: League,
    levelChanged: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(currentLeague.color).copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Session Complete!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(currentLeague.color)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${session.questionsAnswered} questions",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(text = "•", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = session.gameMode,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (levelChanged) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Level ${session.startingLevel}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = if (session.endingLevel > session.startingLevel)
                            Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (session.endingLevel > session.startingLevel)
                            Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Level ${session.endingLevel}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (session.endingLevel > session.startingLevel)
                            Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

@Composable
fun PerformanceSummaryCard(session: GameSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CompactPerformanceMetric(
                icon = Icons.Default.CheckCircle,
                value = "${session.accuracy.roundToInt()}%",
                label = "Accuracy",
                color = if (session.accuracy >= 70) Color(0xFF4CAF50) else Color(0xFFF44336)
            )

            CompactPerformanceMetric(
                icon = Icons.Default.Star,
                value = "+${session.scoreGained}",
                label = "Score",
                color = Color(0xFFFF9800)
            )

            CompactPerformanceMetric(
                icon = Icons.Default.Info,
                value = "${session.averageResponseTime / 1000}s",
                label = "Avg Time",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CompactPerformanceMetric(
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
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LeagueStatusCard(
    currentLeague: League,
    totalScore: Long,
    gameState: GameState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(currentLeague.color).copy(alpha = 0.1f)
        ),
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
                Text(text = currentLeague.icon, fontSize = 24.sp)
                Text(
                    text = "${currentLeague.name} League",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(currentLeague.color)
                )
            }

            Text(
                text = "$totalScore points • Level ${gameState.currentLevel}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Progress to next league
            if (currentLeague.name != "Eternal") {
                val nextLeague = League.leagues.find { it.minScore > currentLeague.minScore }
                if (nextLeague != null) {
                    val progress = ((totalScore - currentLeague.minScore).toFloat() /
                                   (nextLeague.minScore - currentLeague.minScore)).coerceIn(0f, 1f)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Next: ${nextLeague.icon} ${nextLeague.name} (${nextLeague.minScore - totalScore} pts)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(nextLeague.color),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButtonsRow(
    onReturnToMenu: () -> Unit,
    onViewStats: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onReturnToMenu,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Back to Menu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        OutlinedButton(
            onClick = onViewStats,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "View Statistics",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}