package com.example.arithmiticpracticeclaude.data

import java.util.Date

data class League(
    val name: String,
    val minScore: Long,
    val maxScore: Long,
    val color: Long,
    val icon: String,
    val timeLimit: Int,
    val allowedOperations: List<String>,
    val numberRange: IntRange,
    val targetAge: String,
    val description: String
) {
    companion object {
        val leagues = listOf(
            League("Rookie", 0, 999, 0xFF8B4513, "🌱", 25, listOf("Addition"), 1..20, "Ages 8-10", "Simple addition for beginners"),
            League("Bronze III", 1000, 2499, 0xFFCD7F32, "🥉", 22, listOf("Addition"), 1..50, "Ages 9-11", "Addition with larger numbers"),
            League("Bronze II", 2500, 4999, 0xFFCD7F32, "🥉", 20, listOf("Addition", "Subtraction"), 1..50, "Ages 10-12", "Addition and subtraction"),
            League("Bronze I", 5000, 7999, 0xFFCD7F32, "🥉", 18, listOf("Addition", "Subtraction"), 1..100, "Ages 11-13", "Mixed operations up to 100"),
            League("Silver III", 8000, 12999, 0xFFC0C0C0, "🥈", 16, listOf("Addition", "Subtraction", "Fill Blank"), 1..100, "Ages 12-14", "Fill-in-the-blank challenges"),
            League("Silver II", 13000, 19999, 0xFFC0C0C0, "🥈", 15, listOf("Addition", "Subtraction", "Fill Blank"), 1..200, "Ages 13-15", "Larger numbers with gaps"),
            League("Silver I", 20000, 29999, 0xFFC0C0C0, "🥈", 14, listOf("Addition", "Subtraction", "Multiplication"), 1..200, "Ages 14-16", "Introduction to multiplication"),
            League("Gold III", 30000, 44999, 0xFFFFD700, "🥇", 13, listOf("Addition", "Subtraction", "Multiplication"), 1..500, "Ages 15-17", "Multiplication with larger numbers"),
            League("Gold II", 45000, 64999, 0xFFFFD700, "🥇", 12, listOf("Addition", "Subtraction", "Multiplication", "Fill Blank"), 1..500, "Ages 16-18", "Mixed operations including gaps"),
            League("Gold I", 65000, 89999, 0xFFFFD700, "🥇", 11, listOf("Addition", "Subtraction", "Multiplication", "Division"), 1..1000, "Adult", "All basic operations"),
            League("Platinum III", 90000, 124999, 0xFF00CED1, "💎", 10, listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..1000, "Adult+", "Complex mixed operations"),
            League("Platinum II", 125000, 174999, 0xFF00CED1, "💎", 9, listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..2000, "Advanced", "Larger number ranges"),
            League("Platinum I", 175000, 249999, 0xFF00CED1, "💎", 8, listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..5000, "Expert", "Expert-level calculations"),
            League("Diamond III", 250000, 349999, 0xFF40E0D0, "💠", 7, listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..10000, "Genius", "Genius-level arithmetic"),
            League("Diamond II", 350000, 499999, 0xFF40E0D0, "💠", 6, listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..25000, "Genius+", "Extremely challenging numbers"),
            League("Diamond I", 500000, 749999, 0xFF40E0D0, "💠", 6, listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..50000, "Prodigy", "Mathematical prodigy level"),
            League("Master III", 750000, 1099999, 0xFF9932CC, "👑", 5, listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..100000, "Elite", "Elite mathematical skill"),
            League("Master II", 1100000, 1599999, 0xFF9932CC, "👑", 5, listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..250000, "Elite+", "Near-impossible speed"),
            League("Master I", 1600000, 2249999, 0xFF9932CC, "👑", 5, listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..500000, "Superhuman", "Superhuman calculation speed"),
            League("Grandmaster III", 2250000, 3199999, 0xFFFF1493, "🏆", 5, listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..1000000, "Legendary", "Legendary mathematical ability"),
            League("Grandmaster II", 3200000, 4499999, 0xFFFF1493, "🏆", 5, listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..2000000, "Mythical", "Mythical calculation power"),
            League("Grandmaster I", 4500000, 6299999, 0xFFFF1493, "🏆", 5, listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..5000000, "Godlike", "Godlike arithmetic mastery"),
            League("Eternal", 6300000, Long.MAX_VALUE, 0xFFFFFFFF, "♾️", 5, listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..10000000, "Impossible", "Designed to be unbeatable")
        )

        fun getLeagueForScore(score: Long): League {
            return leagues.findLast { score >= it.minScore } ?: leagues.first()
        }

        fun getTimerForScore(score: Long): Int {
            return getLeagueForScore(score).timeLimit
        }

        fun getOperationsForScore(score: Long): List<String> {
            return getLeagueForScore(score).allowedOperations
        }

        fun getNumberRangeForScore(score: Long): IntRange {
            return getLeagueForScore(score).numberRange
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
    val gameMode: String = "Addition",
    val hasCompletedPlacement: Boolean = false,
    val placementScore: Long = 0,
    val initialLeague: String = "Rookie"
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

data class PlacementTest(
    val id: String = java.util.UUID.randomUUID().toString(),
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = 0,
    val questionsAnswered: Int = 0,
    val lastCorrectLevel: Int = 0,
    val failedAtLevel: Int = 0,
    val finalScore: Long = 0,
    val placedInLeague: String = "Rookie",
    val isComplete: Boolean = false
)

data class PlacementLevel(
    val level: Int,
    val name: String,
    val operations: List<String>,
    val numberRange: IntRange,
    val timeLimit: Int,
    val questionsRequired: Int = 3,
    val passThreshold: Float = 0.67f // Need 67% to pass
) {
    companion object {
        val placementLevels = listOf(
            PlacementLevel(1, "Basic Addition", listOf("Addition"), 1..10, 30, 3),
            PlacementLevel(2, "Addition Challenge", listOf("Addition"), 1..25, 25, 3),
            PlacementLevel(3, "Subtraction Intro", listOf("Addition", "Subtraction"), 1..50, 22, 3),
            PlacementLevel(4, "Mixed Basic", listOf("Addition", "Subtraction"), 1..100, 20, 3),
            PlacementLevel(5, "Fill in Blanks", listOf("Addition", "Subtraction", "Fill Blank"), 1..100, 18, 3),
            PlacementLevel(6, "Multiplication Start", listOf("Addition", "Subtraction", "Multiplication"), 1..200, 16, 3),
            PlacementLevel(7, "Mixed Operations", listOf("Addition", "Subtraction", "Multiplication"), 1..500, 14, 3),
            PlacementLevel(8, "Division Intro", listOf("Addition", "Subtraction", "Multiplication", "Division"), 1..500, 12, 3),
            PlacementLevel(9, "All Operations", listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..1000, 10, 3),
            PlacementLevel(10, "Advanced", listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..2000, 8, 3),
            PlacementLevel(11, "Expert", listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..5000, 7, 3),
            PlacementLevel(12, "Genius", listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..10000, 6, 3),
            PlacementLevel(13, "Elite", listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..25000, 5, 3),
            PlacementLevel(14, "Legendary", listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..100000, 5, 3),
            PlacementLevel(15, "Impossible", listOf("Addition", "Subtraction", "Multiplication", "Division", "Fill Blank"), 1..1000000, 5, 3)
        )

        fun getLeagueForPlacementLevel(level: Int): String {
            return when (level) {
                0 -> "Rookie"
                1 -> "Bronze III"
                2 -> "Bronze II"
                3 -> "Bronze I"
                4 -> "Silver III"
                5 -> "Silver II"
                6 -> "Silver I"
                7 -> "Gold III"
                8 -> "Gold II"
                9 -> "Gold I"
                10 -> "Platinum III"
                11 -> "Platinum II"
                12 -> "Platinum I"
                13 -> "Diamond III"
                14 -> "Diamond II"
                15 -> "Diamond I"
                else -> "Master III"
            }
        }
    }
}