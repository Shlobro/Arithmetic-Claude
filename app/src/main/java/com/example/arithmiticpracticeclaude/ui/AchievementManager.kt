package com.example.arithmiticpracticeclaude.ui

import com.example.arithmiticpracticeclaude.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AchievementManager {
    private val _unlockedAchievements = MutableStateFlow<Set<String>>(emptySet())
    val unlockedAchievements: StateFlow<Set<String>> = _unlockedAchievements.asStateFlow()

    private val _newAchievements = MutableStateFlow<List<UnlockedAchievement>>(emptyList())
    val newAchievements: StateFlow<List<UnlockedAchievement>> = _newAchievements.asStateFlow()

    private var sessionsCompleted = 0
    private var perfectSessions = 0
    private var hasAnsweredCorrectly = false
    private var hasLeveledUp = false

    fun checkAchievements(gameState: GameState, session: GameSession?) {
        val newlyUnlocked = mutableListOf<UnlockedAchievement>()

        Achievements.allAchievements.forEach { achievement ->
            if (!_unlockedAchievements.value.contains(achievement.id)) {
                if (checkAchievementRequirement(achievement.requirement, gameState, session)) {
                    unlockAchievement(achievement)
                    newlyUnlocked.add(UnlockedAchievement(achievement))
                }
            }
        }

        if (newlyUnlocked.isNotEmpty()) {
            _newAchievements.value = newlyUnlocked
        }
    }

    private fun checkAchievementRequirement(
        requirement: AchievementRequirement,
        gameState: GameState,
        session: GameSession?
    ): Boolean {
        return when (requirement) {
            is AchievementRequirement.ScoreThreshold -> {
                gameState.totalScore >= requirement.targetScore
            }
            is AchievementRequirement.StreakCount -> {
                gameState.bestStreak >= requirement.targetStreak
            }
            is AchievementRequirement.AccuracyRate -> {
                if (gameState.totalQuestionsAnswered >= 50) {
                    gameState.averageAccuracy >= requirement.targetAccuracy
                } else false
            }
            is AchievementRequirement.LeagueReached -> {
                val currentLeague = League.getLeagueForScore(gameState.totalScore)
                currentLeague.name.contains(requirement.leagueName, ignoreCase = true)
            }
            is AchievementRequirement.PerfectSessions -> {
                perfectSessions >= requirement.count
            }
            is AchievementRequirement.ConsecutiveSessions -> {
                sessionsCompleted >= requirement.count
            }
            is AchievementRequirement.QuestionsAnswered -> {
                gameState.totalQuestionsAnswered >= requirement.count
            }
            AchievementRequirement.FirstSession -> {
                sessionsCompleted >= 1
            }
            AchievementRequirement.FirstCorrectAnswer -> {
                hasAnsweredCorrectly
            }
            AchievementRequirement.FirstLevelUp -> {
                hasLeveledUp
            }
            is AchievementRequirement.TimeBasedAchievement -> {
                // For now, just check the inner requirement
                checkAchievementRequirement(requirement.requirement, gameState, session)
            }
        }
    }

    private fun unlockAchievement(achievement: Achievement) {
        _unlockedAchievements.value = _unlockedAchievements.value + achievement.id
    }

    fun markCorrectAnswer() {
        hasAnsweredCorrectly = true
    }

    fun markLevelUp() {
        hasLeveledUp = true
    }

    fun markSessionComplete(session: GameSession) {
        sessionsCompleted++
        if (session.accuracy >= 100f) {
            perfectSessions++
        }
    }

    fun clearNewAchievements() {
        _newAchievements.value = emptyList()
    }

    fun getAchievementProgress(achievement: Achievement, gameState: GameState): Float {
        return when (val requirement = achievement.requirement) {
            is AchievementRequirement.ScoreThreshold -> {
                (gameState.totalScore.toFloat() / requirement.targetScore).coerceAtMost(1f)
            }
            is AchievementRequirement.StreakCount -> {
                (gameState.bestStreak.toFloat() / requirement.targetStreak).coerceAtMost(1f)
            }
            is AchievementRequirement.QuestionsAnswered -> {
                (gameState.totalQuestionsAnswered.toFloat() / requirement.count).coerceAtMost(1f)
            }
            is AchievementRequirement.PerfectSessions -> {
                (perfectSessions.toFloat() / requirement.count).coerceAtMost(1f)
            }
            is AchievementRequirement.AccuracyRate -> {
                if (gameState.totalQuestionsAnswered >= 50) {
                    (gameState.averageAccuracy / requirement.targetAccuracy).coerceAtMost(1f)
                } else {
                    (gameState.totalQuestionsAnswered.toFloat() / 50f).coerceAtMost(1f)
                }
            }
            else -> {
                if (_unlockedAchievements.value.contains(achievement.id)) 1f else 0f
            }
        }
    }

    fun getAllAchievements(): List<Achievement> {
        return Achievements.allAchievements.map { achievement ->
            achievement.copy(isUnlocked = _unlockedAchievements.value.contains(achievement.id))
        }
    }
}