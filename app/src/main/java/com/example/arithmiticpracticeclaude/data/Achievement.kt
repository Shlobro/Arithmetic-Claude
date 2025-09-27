package com.example.arithmiticpracticeclaude.data

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val category: AchievementCategory,
    val requirement: AchievementRequirement,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0
)

enum class AchievementCategory {
    SCORE,
    STREAK,
    ACCURACY,
    LEAGUE,
    SESSION,
    SPECIAL
}

sealed class AchievementRequirement {
    data class ScoreThreshold(val targetScore: Long) : AchievementRequirement()
    data class StreakCount(val targetStreak: Int) : AchievementRequirement()
    data class AccuracyRate(val targetAccuracy: Float) : AchievementRequirement() // Percentage
    data class LeagueReached(val leagueName: String) : AchievementRequirement()
    data class PerfectSessions(val count: Int) : AchievementRequirement()
    data class ConsecutiveSessions(val count: Int) : AchievementRequirement()
    data class QuestionsAnswered(val count: Long) : AchievementRequirement()
    object FirstSession : AchievementRequirement()
    object FirstCorrectAnswer : AchievementRequirement()
    object FirstLevelUp : AchievementRequirement()
    data class TimeBasedAchievement(val timeInDays: Int, val requirement: AchievementRequirement) : AchievementRequirement()
}

object Achievements {
    val allAchievements = listOf(
        // First time achievements
        Achievement(
            id = "first_session",
            title = "Getting Started",
            description = "Complete your first practice session",
            icon = "🎯",
            category = AchievementCategory.SESSION,
            requirement = AchievementRequirement.FirstSession
        ),
        Achievement(
            id = "first_correct",
            title = "First Success",
            description = "Answer your first question correctly",
            icon = "✅",
            category = AchievementCategory.SPECIAL,
            requirement = AchievementRequirement.FirstCorrectAnswer
        ),
        Achievement(
            id = "first_level_up",
            title = "Level Up",
            description = "Reach level 2 for the first time",
            icon = "⬆️",
            category = AchievementCategory.SPECIAL,
            requirement = AchievementRequirement.FirstLevelUp
        ),

        // Score achievements
        Achievement(
            id = "score_100",
            title = "Century Club",
            description = "Reach a total score of 100 points",
            icon = "💯",
            category = AchievementCategory.SCORE,
            requirement = AchievementRequirement.ScoreThreshold(100)
        ),
        Achievement(
            id = "score_500",
            title = "High Scorer",
            description = "Reach a total score of 500 points",
            icon = "🎖️",
            category = AchievementCategory.SCORE,
            requirement = AchievementRequirement.ScoreThreshold(500)
        ),
        Achievement(
            id = "score_1000",
            title = "Math Master",
            description = "Reach a total score of 1,000 points",
            icon = "🏆",
            category = AchievementCategory.SCORE,
            requirement = AchievementRequirement.ScoreThreshold(1000)
        ),
        Achievement(
            id = "score_5000",
            title = "Arithmetic Expert",
            description = "Reach a total score of 5,000 points",
            icon = "👑",
            category = AchievementCategory.SCORE,
            requirement = AchievementRequirement.ScoreThreshold(5000)
        ),

        // Streak achievements
        Achievement(
            id = "streak_5",
            title = "On a Roll",
            description = "Get 5 correct answers in a row",
            icon = "🔥",
            category = AchievementCategory.STREAK,
            requirement = AchievementRequirement.StreakCount(5)
        ),
        Achievement(
            id = "streak_10",
            title = "Hot Streak",
            description = "Get 10 correct answers in a row",
            icon = "🌟",
            category = AchievementCategory.STREAK,
            requirement = AchievementRequirement.StreakCount(10)
        ),
        Achievement(
            id = "streak_20",
            title = "Unstoppable",
            description = "Get 20 correct answers in a row",
            icon = "⚡",
            category = AchievementCategory.STREAK,
            requirement = AchievementRequirement.StreakCount(20)
        ),

        // League achievements
        Achievement(
            id = "league_bronze",
            title = "Bronze Medalist",
            description = "Reach the Bronze league",
            icon = "🥉",
            category = AchievementCategory.LEAGUE,
            requirement = AchievementRequirement.LeagueReached("Bronze")
        ),
        Achievement(
            id = "league_silver",
            title = "Silver Star",
            description = "Reach the Silver league",
            icon = "🥈",
            category = AchievementCategory.LEAGUE,
            requirement = AchievementRequirement.LeagueReached("Silver")
        ),
        Achievement(
            id = "league_gold",
            title = "Golden Achievement",
            description = "Reach the Gold league",
            icon = "🥇",
            category = AchievementCategory.LEAGUE,
            requirement = AchievementRequirement.LeagueReached("Gold")
        ),
        Achievement(
            id = "league_platinum",
            title = "Platinum Player",
            description = "Reach the Platinum league",
            icon = "💎",
            category = AchievementCategory.LEAGUE,
            requirement = AchievementRequirement.LeagueReached("Platinum")
        ),

        // Session achievements
        Achievement(
            id = "perfect_session",
            title = "Perfect Score",
            description = "Complete a session with 100% accuracy",
            icon = "🎯",
            category = AchievementCategory.SESSION,
            requirement = AchievementRequirement.PerfectSessions(1)
        ),
        Achievement(
            id = "perfect_sessions_5",
            title = "Perfectionist",
            description = "Complete 5 perfect sessions",
            icon = "⭐",
            category = AchievementCategory.SESSION,
            requirement = AchievementRequirement.PerfectSessions(5)
        ),

        // Questions answered achievements
        Achievement(
            id = "questions_100",
            title = "Dedicated Student",
            description = "Answer 100 questions total",
            icon = "📚",
            category = AchievementCategory.SPECIAL,
            requirement = AchievementRequirement.QuestionsAnswered(100)
        ),
        Achievement(
            id = "questions_500",
            title = "Math Enthusiast",
            description = "Answer 500 questions total",
            icon = "🤓",
            category = AchievementCategory.SPECIAL,
            requirement = AchievementRequirement.QuestionsAnswered(500)
        ),
        Achievement(
            id = "questions_1000",
            title = "Question Master",
            description = "Answer 1,000 questions total",
            icon = "🧠",
            category = AchievementCategory.SPECIAL,
            requirement = AchievementRequirement.QuestionsAnswered(1000)
        ),

        // Accuracy achievements
        Achievement(
            id = "accuracy_90",
            title = "Sharp Shooter",
            description = "Maintain 90% accuracy over 50 questions",
            icon = "🎯",
            category = AchievementCategory.ACCURACY,
            requirement = AchievementRequirement.AccuracyRate(90f)
        ),
        Achievement(
            id = "accuracy_95",
            title = "Precision Expert",
            description = "Maintain 95% accuracy over 100 questions",
            icon = "🔍",
            category = AchievementCategory.ACCURACY,
            requirement = AchievementRequirement.AccuracyRate(95f)
        )
    )

    fun getAchievementById(id: String): Achievement? {
        return allAchievements.find { it.id == id }
    }

    fun getAchievementsByCategory(category: AchievementCategory): List<Achievement> {
        return allAchievements.filter { it.category == category }
    }
}

data class UnlockedAchievement(
    val achievement: Achievement,
    val timestamp: Long = System.currentTimeMillis()
)