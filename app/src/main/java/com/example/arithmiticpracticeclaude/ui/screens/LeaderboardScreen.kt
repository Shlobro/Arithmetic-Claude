package com.example.arithmiticpracticeclaude.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arithmiticpracticeclaude.data.*
import com.example.arithmiticpracticeclaude.ui.GameViewModel
import com.example.arithmiticpracticeclaude.ui.GameViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { GameRepository(context) }
    val viewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(repository)
    )
    val gameState by viewModel.gameState.collectAsState()

    // Create mock leaderboard data (in a real app, this would come from a server)
    val leaderboardEntries = remember {
        generateMockLeaderboard(gameState)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        LeaderboardHeader(onBack = onBack)

        // Current Player Stats
        CurrentPlayerCard(gameState = gameState)

        // Leaderboard List
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Global Leaderboard",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(leaderboardEntries) { index, entry ->
                        LeaderboardEntryCard(
                            rank = index + 1,
                            entry = entry,
                            isCurrentPlayer = entry.isCurrentPlayer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }

        Text(
            text = "Leaderboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun CurrentPlayerCard(gameState: GameState) {
    val currentLeague = League.getLeagueForScore(gameState.totalScore)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(currentLeague.color).copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            Color(currentLeague.color)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Your Current Rank",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = currentLeague.icon,
                    style = MaterialTheme.typography.headlineLarge
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentLeague.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(currentLeague.color)
                    )
                    Text(
                        text = "${gameState.totalScore} points",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Progress to next league
            val nextLeague = League.leagues.find { it.minScore > gameState.totalScore }
            nextLeague?.let { next ->
                val progress = (gameState.totalScore - currentLeague.minScore).toFloat() /
                    (next.minScore - currentLeague.minScore)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Progress to ${next.name}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    LinearProgressIndicator(
                        progress = progress.coerceIn(0f, 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = Color(next.color)
                    )
                    Text(
                        text = "${(next.minScore - gameState.totalScore)} points needed",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardEntryCard(
    rank: Int,
    entry: LeaderboardEntry,
    isCurrentPlayer: Boolean
) {
    val backgroundColor = when {
        isCurrentPlayer -> MaterialTheme.colorScheme.primaryContainer
        rank <= 3 -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val rankIcon = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "#$rank"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = if (isCurrentPlayer) CardDefaults.cardElevation(8.dp) else CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Rank
            Text(
                text = rankIcon,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(48.dp),
                textAlign = TextAlign.Center
            )

            // League icon and player info
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = entry.league.icon,
                    style = MaterialTheme.typography.headlineSmall
                )

                Column {
                    Text(
                        text = if (isCurrentPlayer) "You" else entry.playerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isCurrentPlayer) FontWeight.Bold else FontWeight.Medium
                    )
                    Text(
                        text = entry.league.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(entry.league.color)
                    )
                }
            }

            // Score
            Text(
                text = "${entry.score}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

data class LeaderboardEntry(
    val playerName: String,
    val score: Long,
    val league: League,
    val isCurrentPlayer: Boolean = false
)

fun generateMockLeaderboard(currentGameState: GameState): List<LeaderboardEntry> {
    val mockPlayers = listOf(
        "MathGenius2024" to 8500000L,
        "QuickCalculator" to 7800000L,
        "NumberNinja" to 6900000L,
        "ArithmeticAce" to 5400000L,
        "SpeedyMath" to 4200000L,
        "CalculusKing" to 3100000L,
        "MathMaster99" to 2400000L,
        "EquationExpert" to 1800000L,
        "FormulaFan" to 1200000L,
        "MathWhiz123" to 850000L,
        "NumberCruncher" to 650000L,
        "DigitDynamo" to 480000L,
        "MathRocket" to 320000L,
        "CalcChamp" to 220000L,
        "NumberStar" to 150000L
    )

    val entries = mockPlayers.map { (name, score) ->
        LeaderboardEntry(
            playerName = name,
            score = score,
            league = League.getLeagueForScore(score)
        )
    }.toMutableList()

    // Add current player if they would be on leaderboard
    val currentPlayerEntry = LeaderboardEntry(
        playerName = "You",
        score = currentGameState.totalScore,
        league = League.getLeagueForScore(currentGameState.totalScore),
        isCurrentPlayer = true
    )

    entries.add(currentPlayerEntry)

    // Sort by score descending and take top 20
    return entries
        .sortedByDescending { it.score }
        .take(20)
}