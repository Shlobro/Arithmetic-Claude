package com.example.arithmiticpracticeclaude.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arithmiticpracticeclaude.data.*
import com.example.arithmiticpracticeclaude.ui.screens.PlacementPhase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class PlacementViewModel(private val repository: GameRepository) : ViewModel() {

    private val _placementTest = MutableStateFlow<PlacementTest?>(null)
    val placementTest: StateFlow<PlacementTest?> = _placementTest.asStateFlow()

    private val _currentLevel = MutableStateFlow<PlacementLevel?>(null)
    val currentLevel: StateFlow<PlacementLevel?> = _currentLevel.asStateFlow()

    private val _currentQuestion = MutableStateFlow<ArithmeticQuestion?>(null)
    val currentQuestion: StateFlow<ArithmeticQuestion?> = _currentQuestion.asStateFlow()

    private val _showResult = MutableStateFlow(false)
    val showResult: StateFlow<Boolean> = _showResult.asStateFlow()

    private val _lastResult = MutableStateFlow<QuestionResult?>(null)
    val lastResult: StateFlow<QuestionResult?> = _lastResult.asStateFlow()

    private val _isComplete = MutableStateFlow(false)
    val isComplete: StateFlow<Boolean> = _isComplete.asStateFlow()

    private val _currentPhase = MutableStateFlow(PlacementPhase.INTRO)
    val currentPhase: StateFlow<PlacementPhase> = _currentPhase.asStateFlow()

    // Track current level progress
    private var currentLevelIndex = 0
    private var currentLevelQuestions = 0
    private var currentLevelCorrect = 0

    fun startPlacementTest() {
        val test = PlacementTest()
        _placementTest.value = test
        _currentPhase.value = PlacementPhase.TESTING

        // Start with the first placement level
        currentLevelIndex = 0
        currentLevelQuestions = 0
        currentLevelCorrect = 0

        loadLevel(0)
        generateNewQuestion()
    }

    private fun loadLevel(levelIndex: Int) {
        if (levelIndex < PlacementLevel.placementLevels.size) {
            _currentLevel.value = PlacementLevel.placementLevels[levelIndex]
            currentLevelQuestions = 0
            currentLevelCorrect = 0
        }
    }

    fun submitAnswer(userAnswer: Int) {
        val question = _currentQuestion.value ?: return
        val test = _placementTest.value ?: return
        val level = _currentLevel.value ?: return
        val responseTime = System.currentTimeMillis() - question.timestamp

        val isCorrect = userAnswer == question.answer
        currentLevelQuestions++
        if (isCorrect) currentLevelCorrect++

        // Create question result
        val result = QuestionResult(
            question = question.text,
            userAnswer = userAnswer,
            correctAnswer = question.answer,
            isCorrect = isCorrect,
            responseTime = responseTime,
            difficultyLevel = level.level,
            scoreChange = if (isCorrect) level.level * 100 else 0
        )

        _lastResult.value = result
        _showResult.value = true

        // Update placement test
        val updatedTest = test.copy(
            questionsAnswered = test.questionsAnswered + 1,
            lastCorrectLevel = if (isCorrect) maxOf(test.lastCorrectLevel, level.level) else test.lastCorrectLevel,
            failedAtLevel = if (!isCorrect && currentLevelCorrect == 0) level.level else test.failedAtLevel,
            finalScore = test.finalScore + (if (isCorrect) level.level * 100L else 0L)
        )
        _placementTest.value = updatedTest

        // Check if we should advance, repeat, or finish
        if (!isCorrect) {
            // Failed this question - determine final placement
            finishPlacementTest()
        } else if (currentLevelQuestions >= level.questionsRequired) {
            // Completed this level - check if we passed
            val accuracy = currentLevelCorrect.toFloat() / currentLevelQuestions
            if (accuracy >= level.passThreshold) {
                // Passed this level - move to next
                if (currentLevelIndex < PlacementLevel.placementLevels.size - 1) {
                    currentLevelIndex++
                    loadLevel(currentLevelIndex)
                } else {
                    // Completed all levels!
                    finishPlacementTest()
                }
            } else {
                // Failed this level - place at previous level
                finishPlacementTest()
            }
        }
    }

    private fun finishPlacementTest() {
        val test = _placementTest.value ?: return
        val finalLevel = maxOf(0, test.lastCorrectLevel)
        val placedLeague = PlacementLevel.getLeagueForPlacementLevel(finalLevel)

        val completedTest = test.copy(
            endTime = System.currentTimeMillis(),
            placedInLeague = placedLeague,
            isComplete = true
        )

        _placementTest.value = completedTest
        _currentPhase.value = PlacementPhase.COMPLETE
        _isComplete.value = true

        // Save the placement results to game state
        viewModelScope.launch {
            val gameState = GameState(
                hasCompletedPlacement = true,
                placementScore = completedTest.finalScore,
                initialLeague = placedLeague,
                totalScore = completedTest.finalScore
            )
            repository.saveGameState(gameState)
        }
    }

    fun nextQuestion() {
        _showResult.value = false
        _lastResult.value = null

        if (!_isComplete.value) {
            generateNewQuestion()
        }
    }

    private fun generateNewQuestion() {
        val level = _currentLevel.value ?: return

        // Select a random operation from allowed operations
        val operation = level.operations.random()

        val question = when (operation) {
            "Addition" -> generateAdditionQuestion(level)
            "Subtraction" -> generateSubtractionQuestion(level)
            "Multiplication" -> generateMultiplicationQuestion(level)
            "Division" -> generateDivisionQuestion(level)
            "Fill Blank" -> generateFillInBlankQuestion(level)
            else -> generateAdditionQuestion(level)
        }

        _currentQuestion.value = question
    }

    private fun generateAdditionQuestion(level: PlacementLevel): ArithmeticQuestion {
        val a = Random.nextInt(level.numberRange.first, level.numberRange.last + 1)
        val b = Random.nextInt(level.numberRange.first, level.numberRange.last + 1)
        return ArithmeticQuestion("$a + $b = ?", a + b)
    }

    private fun generateSubtractionQuestion(level: PlacementLevel): ArithmeticQuestion {
        val a = Random.nextInt(level.numberRange.first + 5, level.numberRange.last + 1)
        val b = Random.nextInt(level.numberRange.first, a)
        return ArithmeticQuestion("$a - $b = ?", a - b)
    }

    private fun generateMultiplicationQuestion(level: PlacementLevel): ArithmeticQuestion {
        val maxFactor = kotlin.math.sqrt(level.numberRange.last.toDouble()).toInt()
        val a = Random.nextInt(2, maxFactor + 1)
        val b = Random.nextInt(2, maxFactor + 1)
        return ArithmeticQuestion("$a × $b = ?", a * b)
    }

    private fun generateDivisionQuestion(level: PlacementLevel): ArithmeticQuestion {
        val maxFactor = kotlin.math.sqrt(level.numberRange.last.toDouble()).toInt()
        val b = Random.nextInt(2, maxFactor)
        val quotient = Random.nextInt(2, maxFactor)
        val a = b * quotient
        return ArithmeticQuestion("$a ÷ $b = ?", quotient)
    }

    private fun generateFillInBlankQuestion(level: PlacementLevel): ArithmeticQuestion {
        val operation = Random.nextInt(0, 4) // 0: +, 1: -, 2: ×, 3: ÷
        val position = Random.nextInt(0, 3) // 0: first number, 1: second number, 2: result

        return when (operation) {
            0 -> { // Addition
                val a = Random.nextInt(level.numberRange.first, level.numberRange.last + 1)
                val b = Random.nextInt(level.numberRange.first, level.numberRange.last + 1)
                val sum = a + b
                when (position) {
                    0 -> ArithmeticQuestion("? + $b = $sum", a)
                    1 -> ArithmeticQuestion("$a + ? = $sum", b)
                    else -> ArithmeticQuestion("$a + $b = ?", sum)
                }
            }
            1 -> { // Subtraction
                val a = Random.nextInt(level.numberRange.first + 5, level.numberRange.last + 1)
                val b = Random.nextInt(level.numberRange.first, a)
                val result = a - b
                when (position) {
                    0 -> ArithmeticQuestion("? - $b = $result", a)
                    1 -> ArithmeticQuestion("$a - ? = $result", b)
                    else -> ArithmeticQuestion("$a - $b = ?", result)
                }
            }
            2 -> { // Multiplication
                val maxFactor = kotlin.math.sqrt(level.numberRange.last.toDouble()).toInt()
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
                val maxFactor = kotlin.math.sqrt(level.numberRange.last.toDouble()).toInt()
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

class PlacementViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlacementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlacementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}