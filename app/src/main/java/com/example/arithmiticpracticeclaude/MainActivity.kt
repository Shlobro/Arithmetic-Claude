package com.example.arithmiticpracticeclaude

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.arithmiticpracticeclaude.data.GameSession
import com.example.arithmiticpracticeclaude.data.GameState
import com.example.arithmiticpracticeclaude.ui.screens.MainMenuScreen
import com.example.arithmiticpracticeclaude.ui.screens.CompactGameScreen
import com.example.arithmiticpracticeclaude.ui.screens.CompactSessionSummaryScreen
import com.example.arithmiticpracticeclaude.ui.screens.StatisticsScreen
import com.example.arithmiticpracticeclaude.ui.theme.ArithmeticPracticeTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArithmeticPracticeTheme {
                val navController = rememberNavController()
                var completedSession by remember { mutableStateOf<GameSession?>(null) }
                var completedGameState by remember { mutableStateOf<GameState?>(null) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "menu"
                        ) {
                            composable("menu") {
                                MainMenuScreen(
                                    onStartSession = {
                                        navController.navigate("game")
                                    },
                                    onViewStatistics = {
                                        navController.navigate("statistics")
                                    }
                                )
                            }

                            composable("game") {
                                CompactGameScreen(
                                    onSessionComplete = { session, gameState ->
                                        completedSession = session
                                        completedGameState = gameState
                                        navController.navigate("summary")
                                    }
                                )
                            }

                            composable("summary") {
                                if (completedSession != null && completedGameState != null) {
                                    CompactSessionSummaryScreen(
                                        session = completedSession!!,
                                        gameState = completedGameState!!,
                                        onReturnToMenu = {
                                            navController.navigate("menu") {
                                                popUpTo("menu") { inclusive = true }
                                            }
                                        },
                                        onViewStats = {
                                            navController.navigate("statistics")
                                        }
                                    )
                                }
                            }

                            composable("statistics") {
                                StatisticsScreen(
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}