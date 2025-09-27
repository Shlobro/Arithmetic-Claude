package com.example.arithmiticpracticeclaude.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_preferences")

class GameRepository(private val context: Context) {

    private object PreferencesKeys {
        val CURRENT_LEVEL = intPreferencesKey("current_level")
        val TOTAL_SCORE = longPreferencesKey("total_score")
        val TOTAL_QUESTIONS_ANSWERED = longPreferencesKey("total_questions_answered")
        val TOTAL_CORRECT_ANSWERS = longPreferencesKey("total_correct_answers")
        val BEST_STREAK = intPreferencesKey("best_streak")
        val AVERAGE_ACCURACY = floatPreferencesKey("average_accuracy")
        val LAST_SESSION_END = longPreferencesKey("last_session_end")
        val GAME_MODE = stringPreferencesKey("game_mode")
        val SESSIONS_COMPLETED = intPreferencesKey("sessions_completed")
        val TOTAL_PLAY_TIME = longPreferencesKey("total_play_time")
        val HAS_COMPLETED_PLACEMENT = booleanPreferencesKey("has_completed_placement")
        val PLACEMENT_SCORE = longPreferencesKey("placement_score")
        val INITIAL_LEAGUE = stringPreferencesKey("initial_league")
    }

    suspend fun saveGameState(gameState: GameState) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_LEVEL] = gameState.currentLevel
            preferences[PreferencesKeys.TOTAL_SCORE] = gameState.totalScore
            preferences[PreferencesKeys.TOTAL_QUESTIONS_ANSWERED] = gameState.totalQuestionsAnswered
            preferences[PreferencesKeys.TOTAL_CORRECT_ANSWERS] = gameState.totalCorrectAnswers
            preferences[PreferencesKeys.BEST_STREAK] = gameState.bestStreak
            preferences[PreferencesKeys.AVERAGE_ACCURACY] = gameState.averageAccuracy
            preferences[PreferencesKeys.LAST_SESSION_END] = gameState.lastSessionEnd
            preferences[PreferencesKeys.GAME_MODE] = gameState.gameMode
            preferences[PreferencesKeys.HAS_COMPLETED_PLACEMENT] = gameState.hasCompletedPlacement
            preferences[PreferencesKeys.PLACEMENT_SCORE] = gameState.placementScore
            preferences[PreferencesKeys.INITIAL_LEAGUE] = gameState.initialLeague
        }
    }

    fun getGameState(): Flow<GameState> = context.dataStore.data.map { preferences ->
        GameState(
            currentLevel = preferences[PreferencesKeys.CURRENT_LEVEL] ?: 1,
            totalScore = preferences[PreferencesKeys.TOTAL_SCORE] ?: 0L,
            totalQuestionsAnswered = preferences[PreferencesKeys.TOTAL_QUESTIONS_ANSWERED] ?: 0L,
            totalCorrectAnswers = preferences[PreferencesKeys.TOTAL_CORRECT_ANSWERS] ?: 0L,
            bestStreak = preferences[PreferencesKeys.BEST_STREAK] ?: 0,
            averageAccuracy = preferences[PreferencesKeys.AVERAGE_ACCURACY] ?: 0f,
            lastSessionEnd = preferences[PreferencesKeys.LAST_SESSION_END] ?: 0L,
            gameMode = preferences[PreferencesKeys.GAME_MODE] ?: "Addition",
            hasCompletedPlacement = preferences[PreferencesKeys.HAS_COMPLETED_PLACEMENT] ?: false,
            placementScore = preferences[PreferencesKeys.PLACEMENT_SCORE] ?: 0L,
            initialLeague = preferences[PreferencesKeys.INITIAL_LEAGUE] ?: "Rookie"
        )
    }

    suspend fun updateLevel(newLevel: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_LEVEL] = newLevel
        }
    }

    suspend fun updateScore(newScore: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TOTAL_SCORE] = newScore
        }
    }

    suspend fun updateStreakRecord(streak: Int) {
        context.dataStore.edit { preferences ->
            val currentBest = preferences[PreferencesKeys.BEST_STREAK] ?: 0
            if (streak > currentBest) {
                preferences[PreferencesKeys.BEST_STREAK] = streak
            }
        }
    }

    suspend fun completeSession(session: GameSession, gameState: GameState) {
        context.dataStore.edit { preferences ->
            // Update cumulative stats
            val currentTotal = preferences[PreferencesKeys.TOTAL_QUESTIONS_ANSWERED] ?: 0L
            val currentCorrect = preferences[PreferencesKeys.TOTAL_CORRECT_ANSWERS] ?: 0L
            val newTotal = currentTotal + session.questionsAnswered
            val newCorrect = currentCorrect + session.correctAnswers

            preferences[PreferencesKeys.TOTAL_QUESTIONS_ANSWERED] = newTotal
            preferences[PreferencesKeys.TOTAL_CORRECT_ANSWERS] = newCorrect
            preferences[PreferencesKeys.AVERAGE_ACCURACY] = if (newTotal > 0) (newCorrect.toFloat() / newTotal) * 100 else 0f
            preferences[PreferencesKeys.LAST_SESSION_END] = session.endTime
            preferences[PreferencesKeys.CURRENT_LEVEL] = session.endingLevel
            preferences[PreferencesKeys.TOTAL_SCORE] = gameState.totalScore

            // Update session count and play time
            val sessionsCompleted = preferences[PreferencesKeys.SESSIONS_COMPLETED] ?: 0
            val totalPlayTime = preferences[PreferencesKeys.TOTAL_PLAY_TIME] ?: 0L
            preferences[PreferencesKeys.SESSIONS_COMPLETED] = sessionsCompleted + 1
            preferences[PreferencesKeys.TOTAL_PLAY_TIME] = totalPlayTime + session.duration
        }
    }

    suspend fun getStats(): GameStats {
        val preferences = context.dataStore.data.first()
        return GameStats(
            totalScore = preferences[PreferencesKeys.TOTAL_SCORE] ?: 0L,
            totalQuestionsAnswered = preferences[PreferencesKeys.TOTAL_QUESTIONS_ANSWERED] ?: 0L,
            totalCorrectAnswers = preferences[PreferencesKeys.TOTAL_CORRECT_ANSWERS] ?: 0L,
            bestStreak = preferences[PreferencesKeys.BEST_STREAK] ?: 0,
            averageAccuracy = preferences[PreferencesKeys.AVERAGE_ACCURACY] ?: 0f,
            sessionsCompleted = preferences[PreferencesKeys.SESSIONS_COMPLETED] ?: 0,
            totalPlayTime = preferences[PreferencesKeys.TOTAL_PLAY_TIME] ?: 0L
        )
    }
}

data class GameStats(
    val totalScore: Long,
    val totalQuestionsAnswered: Long,
    val totalCorrectAnswers: Long,
    val bestStreak: Int,
    val averageAccuracy: Float,
    val sessionsCompleted: Int,
    val totalPlayTime: Long
)