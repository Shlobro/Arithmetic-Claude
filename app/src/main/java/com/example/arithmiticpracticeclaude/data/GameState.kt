package com.example.arithmiticpracticeclaude.data

import java.util.Date

data class League(
    val name: String,
    val minScore: Long,
    val maxScore: Long,
    val color: Long,
    val icon: String
) {
    companion object {
        val leagues = listOf(
            League("Rookie", 0, 199, 0xFF8B4513, "🌱"),
            League("Bronze III", 200, 399, 0xFFCD7F32, "🥉"),
            League("Bronze II", 400, 599, 0xFFCD7F32, "🥉"),
            League("Bronze I", 600, 799, 0xFFCD7F32, "🥉"),
            League("Silver III", 800, 1199, 0xFFC0C0C0, "🥈"),
            League("Silver II", 1200, 1599, 0xFFC0C0C0, "🥈"),
            League("Silver I", 1600, 1999, 0xFFC0C0C0, "🥈"),
            League("Gold III", 2000, 2799, 0xFFFFD700, "🥇"),
            League("Gold II", 2800, 3599, 0xFFFFD700, "🥇"),
            League("Gold I", 3600, 4399, 0xFFFFD700, "🥇"),
            League("Platinum III", 4400, 5999, 0xFF00CED1, "💎"),
            League("Platinum II", 6000, 7599, 0xFF00CED1, "💎"),
            League("Platinum I", 7600, 9199, 0xFF00CED1, "💎"),
            League("Diamond III", 9200, 12999, 0xFF40E0D0, "💠"),
            League("Diamond II", 13000, 16799, 0xFF40E0D0, "💠"),
            League("Diamond I", 16800, 20599, 0xFF40E0D0, "💠"),
            League("Master III", 20600, 27999, 0xFF9932CC, "👑"),
            League("Master II", 28000, 35999, 0xFF9932CC, "👑"),
            League("Master I", 36000, 44999, 0xFF9932CC, "👑"),
            League("Grandmaster III", 45000, 59999, 0xFFFF1493, "🏆"),
            League("Grandmaster II", 60000, 79999, 0xFFFF1493, "🏆"),
            League("Grandmaster I", 80000, 99999, 0xFFFF1493, "🏆"),
            League("Champion III", 100000, 139999, 0xFFFF0000, "⭐"),
            League("Champion II", 140000, 189999, 0xFFFF0000, "⭐"),
            League("Champion I", 190000, 249999, 0xFFFF0000, "⭐"),
            League("Legend III", 250000, 349999, 0xFF800080, "🌟"),
            League("Legend II", 350000, 499999, 0xFF800080, "🌟"),
            League("Legend I", 500000, 699999, 0xFF800080, "🌟"),
            League("Mythic III", 700000, 999999, 0xFF000000, "🔥"),
            League("Mythic II", 1000000, 1499999, 0xFF000000, "🔥"),
            League("Mythic I", 1500000, 2499999, 0xFF000000, "🔥"),
            League("Eternal", 2500000, Long.MAX_VALUE, 0xFFFFFFFF, "♾️")
        )

        fun getLeagueForScore(score: Long): League {
            return leagues.findLast { score >= it.minScore } ?: leagues.first()
        }

        fun getTimerForScore(score: Long): Int {
            return when {
                score < 200 -> 30      // Rookie: 30s
                score < 800 -> 28      // Bronze: 28s
                score < 2000 -> 25     // Silver: 25s
                score < 4400 -> 22     // Gold: 22s
                score < 9200 -> 20     // Platinum: 20s
                score < 20600 -> 18    // Diamond: 18s
                score < 45000 -> 15    // Master: 15s
                score < 100000 -> 12   // Grandmaster: 12s
                score < 250000 -> 10   // Champion: 10s
                score < 700000 -> 8    // Legend: 8s
                score < 2500000 -> 6   // Mythic: 6s
                else -> 5              // Eternal: 5s
            }
        }
    }
}

data class GameState(
    val currentLevel: Int = 1,
    val totalScore: Long = 0,
    val currentSessionScore: Int = 0,
    val currentSessionQuestions: Int = 0,
    val currentSessionCorrect: Int = 0,
    val sessionStartTime: Long = System.currentTimeMillis(),
    val lastSessionEnd: Long = 0,
    val totalQuestionsAnswered: Long = 0,
    val totalCorrectAnswers: Long = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val averageAccuracy: Float = 0f,
    val gameMode: String = "Addition"
)

data class GameSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = 0,
    val startingLevel: Int,
    val endingLevel: Int,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val scoreGained: Int = 0,
    val gameMode: String,
    val averageResponseTime: Long = 0
) {
    val duration: Long get() = if (endTime > 0) endTime - startTime else System.currentTimeMillis() - startTime
    val accuracy: Float get() = if (questionsAnswered > 0) (correctAnswers.toFloat() / questionsAnswered) * 100 else 0f
    val isActive: Boolean get() = endTime == 0L
}

data class QuestionResult(
    val question: String,
    val userAnswer: Int?,
    val correctAnswer: Int,
    val isCorrect: Boolean,
    val responseTime: Long,
    val difficultyLevel: Int,
    val scoreChange: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class DynamicDifficulty(
    val level: Int,
    val baseRange: IntRange,
    val multiplierRange: IntRange,
    val bonusMultiplier: Float,
    val timeLimit: Int = 30
) {
    companion object {
        fun fromLevel(level: Int): DynamicDifficulty {
            return when {
                level <= 5 -> DynamicDifficulty(
                    level = level,
                    baseRange = 1..(5 + level * 2),
                    multiplierRange = 2..(5 + level),
                    bonusMultiplier = 1.0f + (level * 0.1f),
                    timeLimit = maxOf(15, 35 - level)
                )
                level <= 15 -> DynamicDifficulty(
                    level = level,
                    baseRange = 1..(10 + level * 3),
                    multiplierRange = 2..(7 + level),
                    bonusMultiplier = 1.0f + (level * 0.15f),
                    timeLimit = maxOf(10, 30 - (level - 5))
                )
                level <= 30 -> DynamicDifficulty(
                    level = level,
                    baseRange = 1..(20 + level * 4),
                    multiplierRange = 2..(10 + level),
                    bonusMultiplier = 1.0f + (level * 0.2f),
                    timeLimit = maxOf(8, 25 - (level - 15))
                )
                else -> DynamicDifficulty(
                    level = level,
                    baseRange = 1..(50 + level * 5),
                    multiplierRange = 2..(15 + level),
                    bonusMultiplier = 1.0f + (level * 0.25f),
                    timeLimit = maxOf(5, 20 - (level - 30) / 2)
                )
            }
        }
    }
}