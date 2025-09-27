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
        // Auto-select game mode based on level
        val autoSelectedMode = selectGameModeForLevel(currentState.currentLevel)

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

    private fun selectGameModeForLevel(level: Int): String {
        return when {
            level <= 2 -> "Addition"
            level <= 4 -> if (Random.nextBoolean()) "Addition" else "Subtraction"
            level <= 6 -> when (Random.nextInt(3)) {
                0 -> "Addition"
                1 -> "Subtraction"
                else -> "Fill Blank"
            }
            level <= 8 -> when (Random.nextInt(4)) {
                0 -> "Addition"
                1 -> "Subtraction"
                2 -> "Fill Blank"
                else -> "Multiplication"
            }
            level <= 12 -> when (Random.nextInt(5)) {
                0 -> "Addition"
                1 -> "Subtraction"
                2 -> "Fill Blank"
                3 -> "Multiplication"
                else -> "Division"
            }
            else -> when (Random.nextInt(5)) {
                0 -> "Addition"
                1 -> "Subtraction"
                2 -> "Fill Blank"
                3 -> "Multiplication"
                else -> "Division"
            }
        }
    }

    fun submitAnswer(userAnswer: Int) {
        val question = _currentQuestion.value ?: return
        val session = _currentSession.value ?: return
        val responseTime = System.currentTimeMillis() - question.timestamp

        val isCorrect = userAnswer == question.answer
        val currentState = _gameState.value
        val difficulty = DynamicDifficulty.fromLevel(currentState.currentLevel)

        // Calculate score change
        val baseScore = if (isCorrect) {
            (10 * difficulty.bonusMultiplier).toInt()
        } else {
            -5 // Penalty for wrong answer
        }

        val timeBonus = if (isCorrect && responseTime < 10000) { // Under 10 seconds
            ((10000 - responseTime) / 1000).toInt() * 2
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

        _gameState.value = currentState.copy(
            currentLevel = newLevel,
            totalScore = newTotalScore,
            currentSessionScore = newScore,
            currentSessionQuestions = newQuestionsAnswered,
            currentSessionCorrect = newCorrectAnswers,
            currentStreak = newStreak,
            bestStreak = newBestStreak
        )

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
        val difficulty = DynamicDifficulty.fromLevel(currentState.currentLevel)

        // Auto-select mode for each question based on current level
        val selectedMode = selectGameModeForLevel(currentState.currentLevel)

        val question = when (selectedMode) {
            "Addition" -> generateAdditionQuestion(difficulty)
            "Subtraction" -> generateSubtractionQuestion(difficulty)
            "Multiplication" -> generateMultiplicationQuestion(difficulty)
            "Division" -> generateDivisionQuestion(difficulty)
            "Fill Blank" -> generateFillInBlankQuestion(difficulty)
            else -> generateAdditionQuestion(difficulty)
        }

        // Update the gameMode in state to reflect current question
        _gameState.value = currentState.copy(gameMode = selectedMode)

        _currentQuestion.value = question
    }


    fun endSession() {
        val session = _currentSession.value ?: return
        val endedSession = session.copy(endTime = System.currentTimeMillis())

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
    private fun generateAdditionQuestion(difficulty: DynamicDifficulty): ArithmeticQuestion {
        val a = Random.nextInt(difficulty.baseRange.first, difficulty.baseRange.last + 1)
        val b = Random.nextInt(difficulty.baseRange.first, difficulty.baseRange.last + 1)
        return ArithmeticQuestion("$a + $b = ?", a + b)
    }

    private fun generateSubtractionQuestion(difficulty: DynamicDifficulty): ArithmeticQuestion {
        val a = Random.nextInt(difficulty.baseRange.first + 5, difficulty.baseRange.last + 1)
        val b = Random.nextInt(difficulty.baseRange.first, a)
        return ArithmeticQuestion("$a - $b = ?", a - b)
    }

    private fun generateMultiplicationQuestion(difficulty: DynamicDifficulty): ArithmeticQuestion {
        val a = Random.nextInt(difficulty.multiplierRange.first, difficulty.multiplierRange.last + 1)
        val b = Random.nextInt(difficulty.multiplierRange.first, difficulty.multiplierRange.last + 1)
        return ArithmeticQuestion("$a × $b = ?", a * b)
    }

    private fun generateDivisionQuestion(difficulty: DynamicDifficulty): ArithmeticQuestion {
        val b = Random.nextInt(2, difficulty.multiplierRange.last)
        val quotient = Random.nextInt(2, difficulty.baseRange.last / 2 + 1)
        val a = b * quotient
        return ArithmeticQuestion("$a ÷ $b = ?", quotient)
    }

    private fun generateFillInBlankQuestion(difficulty: DynamicDifficulty): ArithmeticQuestion {
        val operation = Random.nextInt(0, 4) // 0: +, 1: -, 2: ×, 3: ÷
        val position = Random.nextInt(0, 3) // 0: first number, 1: second number, 2: result

        return when (operation) {
            0 -> { // Addition
                val a = Random.nextInt(difficulty.baseRange.first, difficulty.baseRange.last + 1)
                val b = Random.nextInt(difficulty.baseRange.first, difficulty.baseRange.last + 1)
                val sum = a + b
                when (position) {
                    0 -> ArithmeticQuestion("? + $b = $sum", a)
                    1 -> ArithmeticQuestion("$a + ? = $sum", b)
                    else -> ArithmeticQuestion("$a + $b = ?", sum)
                }
            }
            1 -> { // Subtraction
                val a = Random.nextInt(difficulty.baseRange.first + 5, difficulty.baseRange.last + 1)
                val b = Random.nextInt(difficulty.baseRange.first, a)
                val result = a - b
                when (position) {
                    0 -> ArithmeticQuestion("? - $b = $result", a)
                    1 -> ArithmeticQuestion("$a - ? = $result", b)
                    else -> ArithmeticQuestion("$a - $b = ?", result)
                }
            }
            2 -> { // Multiplication
                val a = Random.nextInt(difficulty.multiplierRange.first, difficulty.multiplierRange.last + 1)
                val b = Random.nextInt(difficulty.multiplierRange.first, difficulty.multiplierRange.last + 1)
                val product = a * b
                when (position) {
                    0 -> ArithmeticQuestion("? × $b = $product", a)
                    1 -> ArithmeticQuestion("$a × ? = $product", b)
                    else -> ArithmeticQuestion("$a × $b = ?", product)
                }
            }
            else -> { // Division
                val b = Random.nextInt(2, difficulty.multiplierRange.last)
                val quotient = Random.nextInt(2, difficulty.baseRange.last / 2 + 1)
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