package com.example.arithmiticpracticeclaude.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arithmiticpracticeclaude.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _currentSession = MutableStateFlow<GameSession?>(null)
    val currentSession: StateFlow<GameSession?> = _currentSession.asStateFlow()

    private val _currentQuestion = MutableStateFlow<ArithmeticQuestion?>(null)
    val currentQuestion: StateFlow<ArithmeticQuestion?> = _currentQuestion.asStateFlow()

    private val _showResult = MutableStateFlow(false)
    val showResult: StateFlow<Boolean> = _showResult.asStateFlow()

    private val _lastResult = MutableStateFlow<QuestionResult?>(null)
    val lastResult: StateFlow<QuestionResult?> = _lastResult.asStateFlow()

    private val _sessionEnded = MutableStateFlow(false)
    val sessionEnded: StateFlow<Boolean> = _sessionEnded.asStateFlow()

    val achievementManager = AchievementManager()
    val newAchievements: StateFlow<List<UnlockedAchievement>> = achievementManager.newAchievements

    init {
        loadGameState()
    }

    private fun loadGameState() {
        viewModelScope.launch {
            repository.getGameState().collect { state ->
                val currentState = _gameState.value
                // Don't overwrite session state if we're in the middle of a session
                if (_currentSession.value != null && currentState.currentSessionQuestions > 0) {
                    // Only update persistent fields, keep session fields intact
                    _gameState.value = state.copy(
                        currentSessionScore = currentState.currentSessionScore,
                        currentSessionQuestions = currentState.currentSessionQuestions,
                        currentSessionCorrect = currentState.currentSessionCorrect,
                        sessionStartTime = currentState.sessionStartTime,
                        currentStreak = currentState.currentStreak
                    )
                } else {
                    _gameState.value = state
                    if (_currentQuestion.value == null) {
                        generateNewQuestion()
                    }
                }
            }
        }
    }

    fun startNewSession() {
        val currentState = _gameState.value
        // Auto-select game mode based on total score (league)
        val autoSelectedMode = selectGameModeForLevel(currentState.totalScore)

        val session = GameSession(
            startingLevel = currentState.currentLevel,
            endingLevel = currentState.currentLevel,
            gameMode = autoSelectedMode
        )
        _currentSession.value = session
        _sessionEnded.value = false
        _gameState.value = currentState.copy(
            currentSessionScore = 0,
            currentSessionQuestions = 0,
            currentSessionCorrect = 0,
            sessionStartTime = System.currentTimeMillis(),
            gameMode = autoSelectedMode
        )
        generateNewQuestion()
    }

    private fun selectGameModeForLevel(totalScore: Long): String {
        val allowedOperations = League.getOperationsForScore(totalScore)
        return allowedOperations.random()
    }

    fun submitAnswer(userAnswer: Int) {
        val question = _currentQuestion.value ?: return
        val session = _currentSession.value ?: return
        val responseTime = System.currentTimeMillis() - question.timestamp

        val isCorrect = userAnswer == question.answer
        val currentState = _gameState.value
        val league = League.getLeagueForScore(currentState.totalScore)

        val baseScore = if (isCorrect) {
            // Higher leagues give more points per correct answer
            when {
                league.name.contains("Rookie") -> 10
                league.name.contains("Bronze") -> 25
                league.name.contains("Silver") -> 50
                league.name.contains("Gold") -> 100
                league.name.contains("Platinum") -> 200
                league.name.contains("Diamond") -> 500
                league.name.contains("Master") -> 1000
                league.name.contains("Grandmaster") -> 2500
                league.name.contains("Eternal") -> 5000
                else -> 50
            }
        } else {
            // Small penalty for wrong answers (to prevent abuse)
            -5
        }

        // Massive time bonus for fast answers in higher leagues
        val timeBonus = if (isCorrect && responseTime < league.timeLimit * 800) { // Under 80% of time limit
            val speedMultiplier = when {
                league.name.contains("Rookie") -> 1
                league.name.contains("Bronze") -> 2
                league.name.contains("Silver") -> 3
                league.name.contains("Gold") -> 5
                league.name.contains("Platinum") -> 8
                league.name.contains("Diamond") -> 15
                league.name.contains("Master") -> 25
                league.name.contains("Grandmaster") -> 50
                league.name.contains("Eternal") -> 100
                else -> 1
            }
            val timeLeftRatio = (league.timeLimit * 1000 - responseTime).toFloat() / (league.timeLimit * 1000)
            (baseScore * timeLeftRatio * speedMultiplier).toInt()
        } else 0

        val scoreChange = baseScore + timeBonus

        // Update game state
        val newScore = maxOf(0, currentState.currentSessionScore + scoreChange)
        val newTotalScore = maxOf(0, currentState.totalScore + scoreChange)
        val newCorrectAnswers = if (isCorrect) currentState.currentSessionCorrect + 1 else currentState.currentSessionCorrect
        val newQuestionsAnswered = currentState.currentSessionQuestions + 1

        // Calculate new level based on performance
        val newLevel = calculateNewLevel(currentState.currentLevel, isCorrect, newScore, newQuestionsAnswered)

        // Update streak
        val newStreak = if (isCorrect) currentState.currentStreak + 1 else 0
        val newBestStreak = maxOf(currentState.bestStreak, newStreak)

        val updatedGameState = currentState.copy(
            currentLevel = newLevel,
            totalScore = newTotalScore,
            currentSessionScore = newScore,
            currentSessionQuestions = newQuestionsAnswered,
            currentSessionCorrect = newCorrectAnswers,
            currentStreak = newStreak,
            bestStreak = newBestStreak,
            totalQuestionsAnswered = currentState.totalQuestionsAnswered + 1,
            totalCorrectAnswers = if (isCorrect) currentState.totalCorrectAnswers + 1 else currentState.totalCorrectAnswers,
            averageAccuracy = if (currentState.totalQuestionsAnswered + 1 > 0) {
                ((if (isCorrect) currentState.totalCorrectAnswers + 1 else currentState.totalCorrectAnswers).toFloat() /
                 (currentState.totalQuestionsAnswered + 1)) * 100
            } else 0f
        )

        _gameState.value = updatedGameState

        // Check achievements
        if (isCorrect) {
            achievementManager.markCorrectAnswer()
        }
        if (newLevel > currentState.currentLevel) {
            achievementManager.markLevelUp()
        }
        achievementManager.checkAchievements(updatedGameState, session)

        // Create question result
        val result = QuestionResult(
            question = question.text,
            userAnswer = userAnswer,
            correctAnswer = question.answer,
            isCorrect = isCorrect,
            responseTime = responseTime,
            difficultyLevel = currentState.currentLevel,
            scoreChange = scoreChange
        )

        _lastResult.value = result

        // Update session
        _currentSession.value = session.copy(
            endingLevel = newLevel,
            questionsAnswered = newQuestionsAnswered,
            correctAnswers = newCorrectAnswers,
            scoreGained = newScore,
            averageResponseTime = (session.averageResponseTime * (newQuestionsAnswered - 1) + responseTime) / newQuestionsAnswered
        )

        _showResult.value = true

        // End session after 10 questions
        if (newQuestionsAnswered >= 10) {
            // Don't generate a new question, just end the session
            endSession()
            return
        }

        // Save state after significant milestones
        if (newQuestionsAnswered % 5 == 0 || newStreak > currentState.bestStreak) {
            saveGameState()
        }
    }

    private fun calculateNewLevel(currentLevel: Int, isCorrect: Boolean, sessionScore: Int, questionsAnswered: Int): Int {
        if (questionsAnswered < 3) return currentLevel // Wait for a few questions before adjusting

        val accuracy = if (questionsAnswered > 0) {
            (_gameState.value.currentSessionCorrect.toFloat() / questionsAnswered) * 100
        } else 0f

        return when {
            // Level up conditions: high accuracy and good score
            accuracy >= 80f && sessionScore >= currentLevel * 50 && isCorrect -> {
                minOf(100, currentLevel + 1) // Cap at level 100
            }
            // Level down conditions: low accuracy or negative score
            accuracy < 40f || sessionScore < currentLevel * 10 -> {
                maxOf(1, currentLevel - 1) // Floor at level 1
            }
            else -> currentLevel
        }
    }

    fun nextQuestion() {
        _showResult.value = false
        _lastResult.value = null
        generateNewQuestion()
    }

    private fun generateNewQuestion() {
        val currentState = _gameState.value
        val league = League.getLeagueForScore(currentState.totalScore)
        val numberRange = league.numberRange

        // Auto-select mode for each question based on current league
        val selectedMode = selectGameModeForLevel(currentState.totalScore)

        val question = when (selectedMode) {
            "Addition" -> generateAdditionQuestion(numberRange)
            "Subtraction" -> generateSubtractionQuestion(numberRange)
            "Multiplication" -> generateMultiplicationQuestion(numberRange)
            "Division" -> generateDivisionQuestion(numberRange)
            "Fill Blank" -> generateFillInBlankQuestion(numberRange)
            else -> generateAdditionQuestion(numberRange)
        }

        // Update the gameMode in state to reflect current question
        _gameState.value = currentState.copy(gameMode = selectedMode)

        _currentQuestion.value = question
    }


    fun endSession() {
        val session = _currentSession.value ?: return
        val endedSession = session.copy(endTime = System.currentTimeMillis())

        // Mark session completion for achievements
        achievementManager.markSessionComplete(endedSession)
        achievementManager.checkAchievements(_gameState.value, endedSession)

        viewModelScope.launch {
            repository.completeSession(endedSession, _gameState.value)
            _sessionEnded.value = true
            _currentSession.value = endedSession
        }
    }

    private fun saveGameState() {
        viewModelScope.launch {
            repository.saveGameState(_gameState.value)
        }
    }

    // Question generation functions
    private fun generateAdditionQuestion(numberRange: IntRange): ArithmeticQuestion {
        val a = Random.nextInt(numberRange.first, numberRange.last + 1)
        val b = Random.nextInt(numberRange.first, numberRange.last + 1)
        return ArithmeticQuestion("$a + $b = ?", a + b)
    }

    private fun generateSubtractionQuestion(numberRange: IntRange): ArithmeticQuestion {
        val a = Random.nextInt(numberRange.first + 5, numberRange.last + 1)
        val b = Random.nextInt(numberRange.first, a)
        return ArithmeticQuestion("$a - $b = ?", a - b)
    }

    private fun generateMultiplicationQuestion(numberRange: IntRange): ArithmeticQuestion {
        // For multiplication, use smaller factors to avoid extremely large products
        val maxFactor = kotlin.math.sqrt(numberRange.last.toDouble()).toInt()
        val a = Random.nextInt(2, maxFactor + 1)
        val b = Random.nextInt(2, maxFactor + 1)
        return ArithmeticQuestion("$a × $b = ?", a * b)
    }

    private fun generateDivisionQuestion(numberRange: IntRange): ArithmeticQuestion {
        val maxFactor = kotlin.math.sqrt(numberRange.last.toDouble()).toInt()
        val b = Random.nextInt(2, maxFactor)
        val quotient = Random.nextInt(2, maxFactor)
        val a = b * quotient
        return ArithmeticQuestion("$a ÷ $b = ?", quotient)
    }

    private fun generateFillInBlankQuestion(numberRange: IntRange): ArithmeticQuestion {
        val operation = Random.nextInt(0, 4) // 0: +, 1: -, 2: ×, 3: ÷
        val position = Random.nextInt(0, 3) // 0: first number, 1: second number, 2: result

        return when (operation) {
            0 -> { // Addition
                val a = Random.nextInt(numberRange.first, numberRange.last + 1)
                val b = Random.nextInt(numberRange.first, numberRange.last + 1)
                val sum = a + b
                when (position) {
                    0 -> ArithmeticQuestion("? + $b = $sum", a)
                    1 -> ArithmeticQuestion("$a + ? = $sum", b)
                    else -> ArithmeticQuestion("$a + $b = ?", sum)
                }
            }
            1 -> { // Subtraction
                val a = Random.nextInt(numberRange.first + 5, numberRange.last + 1)
                val b = Random.nextInt(numberRange.first, a)
                val result = a - b
                when (position) {
                    0 -> ArithmeticQuestion("? - $b = $result", a)
                    1 -> ArithmeticQuestion("$a - ? = $result", b)
                    else -> ArithmeticQuestion("$a - $b = ?", result)
                }
            }
            2 -> { // Multiplication
                val maxFactor = kotlin.math.sqrt(numberRange.last.toDouble()).toInt()
                val a = Random.nextInt(2, maxFactor + 1)
                val b = Random.nextInt(2, maxFactor + 1)
                val product = a * b
                when (position) {
                    0 -> ArithmeticQuestion("? × $b = $product", a)
                    1 -> ArithmeticQuestion("$a × ? = $product", b)
                    else -> ArithmeticQuestion("$a × $b = ?", product)
                }
            }
            else -> { // Division
                val maxFactor = kotlin.math.sqrt(numberRange.last.toDouble()).toInt()
                val b = Random.nextInt(2, maxFactor)
                val quotient = Random.nextInt(2, maxFactor)
                val a = b * quotient
                when (position) {
                    0 -> ArithmeticQuestion("? ÷ $b = $quotient", a)
                    1 -> ArithmeticQuestion("$a ÷ ? = $quotient", b)
                    else -> ArithmeticQuestion("$a ÷ $b = ?", quotient)
                }
            }
        }
    }
}

data class ArithmeticQuestion(
    val text: String,
    val answer: Int,
    val timestamp: Long = System.currentTimeMillis()
)

class GameViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}