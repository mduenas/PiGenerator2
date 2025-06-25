package com.markduenas.android.apigen.memorization

import com.markduenas.android.apigen.calculation.PiCalculator
import com.markduenas.android.apigen.data.*
import com.markduenas.android.apigen.utils.getCurrentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MemorizationGame {
    private val _gameState = MutableStateFlow(MemorizationGameState())
    val gameState: StateFlow<MemorizationGameState> = _gameState.asStateFlow()
    
    private val _stats = MutableStateFlow(MemorizationStats(
        totalDigitsMemorized = 0,
        personalBest = 0,
        averageAccuracy = 0f,
        totalSessions = 0,
        averageSessionTime = 0L,
        achievementsUnlocked = emptyList()
    ))
    val stats: StateFlow<MemorizationStats> = _stats.asStateFlow()
    
    suspend fun startSession(mode: MemorizationMode, startDigit: Int = 0) {
        val rawDigits = PiCalculator.getDigitsString(startDigit, 100)
        // Filter out non-digit characters (like decimal points)
        val targetDigits = rawDigits.filter { it.isDigit() }
        _gameState.value = MemorizationGameState(
            mode = mode,
            startDigit = startDigit,
            targetDigits = targetDigits,
            currentPosition = 0,
            isActive = true,
            startTime = getCurrentTimeMillis()
        )
    }
    
    fun submitAnswer(digit: String): Boolean {
        val currentState = _gameState.value
        if (!currentState.isActive) return false
        
        val expectedDigit = currentState.targetDigits.getOrNull(currentState.currentPosition)?.toString()
        val isCorrect = digit == expectedDigit
        
        val newCorrectAnswers = if (isCorrect) currentState.correctAnswers + 1 else currentState.correctAnswers
        val newTotalAttempts = currentState.totalAttempts + 1
        val newPosition = if (isCorrect) currentState.currentPosition + 1 else currentState.currentPosition
        
        _gameState.value = currentState.copy(
            currentPosition = newPosition,
            correctAnswers = newCorrectAnswers,
            totalAttempts = newTotalAttempts,
            lastAnswer = digit,
            lastAnswerCorrect = isCorrect
        )
        
        // Check if session is complete
        if (newPosition >= currentState.targetDigits.length || (!isCorrect && currentState.mode == MemorizationMode.TEST)) {
            endSession()
        }
        
        return isCorrect
    }
    
    fun endSession() {
        val currentState = _gameState.value
        if (!currentState.isActive) return
        
        val endTime = getCurrentTimeMillis()
        val sessionTime = endTime - currentState.startTime
        
        val session = MemorizationSession(
            id = generateSessionId(),
            startDigit = currentState.startDigit,
            endDigit = currentState.startDigit + currentState.currentPosition,
            correctAnswers = currentState.correctAnswers,
            totalAttempts = currentState.totalAttempts,
            timeSpentMs = sessionTime,
            date = endTime,
            mode = currentState.mode
        )
        
        _gameState.value = currentState.copy(
            isActive = false,
            isComplete = true,
            endTime = endTime
        )
        
        updateStats(session)
    }
    
    fun resetGame() {
        _gameState.value = MemorizationGameState()
    }
    
    private fun updateStats(session: MemorizationSession) {
        val currentStats = _stats.value
        val newPersonalBest = maxOf(currentStats.personalBest, session.correctAnswers)
        val newTotalSessions = currentStats.totalSessions + 1
        val newTotalDigits = currentStats.totalDigitsMemorized + session.correctAnswers
        
        val newAverageAccuracy = if (session.totalAttempts > 0) {
            (currentStats.averageAccuracy * currentStats.totalSessions + 
             (session.correctAnswers.toFloat() / session.totalAttempts)) / newTotalSessions
        } else currentStats.averageAccuracy
        
        val newAverageSessionTime = (currentStats.averageSessionTime * currentStats.totalSessions + 
                                   session.timeSpentMs) / newTotalSessions
        
        val newAchievements = checkForNewAchievements(newPersonalBest, newTotalDigits, newTotalSessions)
        
        _stats.value = currentStats.copy(
            totalDigitsMemorized = newTotalDigits,
            personalBest = newPersonalBest,
            averageAccuracy = newAverageAccuracy,
            totalSessions = newTotalSessions,
            averageSessionTime = newAverageSessionTime,
            achievementsUnlocked = currentStats.achievementsUnlocked + newAchievements
        )
    }
    
    private fun checkForNewAchievements(personalBest: Int, totalDigits: Int, totalSessions: Int): List<Achievement> {
        val achievements = mutableListOf<Achievement>()
        val currentTime = getCurrentTimeMillis()
        
        val milestones = listOf(
            10 to "First Steps",
            50 to "Getting Started",
            100 to "Century Club",
            250 to "Pi Master",
            500 to "Pi Expert",
            1000 to "Pi Legend"
        )
        
        milestones.forEach { (threshold, title) ->
            if (personalBest >= threshold && !_stats.value.achievementsUnlocked.any { it.threshold == threshold }) {
                achievements.add(Achievement(
                    id = "digits_$threshold",
                    title = title,
                    description = "Memorized $threshold digits of Pi in a single session",
                    threshold = threshold,
                    isUnlocked = true,
                    unlockedDate = currentTime
                ))
            }
        }
        
        return achievements
    }
    
    private fun generateSessionId(): String = getCurrentTimeMillis().toString()
    
    fun getHint(position: Int): String {
        val currentState = _gameState.value
        return if (position < currentState.targetDigits.length) {
            currentState.targetDigits[position].toString()
        } else ""
    }
    
    fun getColorForDigit(digit: Int): String {
        // Color coding for memory aids
        return when (digit) {
            0 -> "#FF0000" // Red
            1 -> "#FF8000" // Orange
            2 -> "#FFFF00" // Yellow
            3 -> "#80FF00" // Lime
            4 -> "#00FF00" // Green
            5 -> "#00FF80" // Spring Green
            6 -> "#00FFFF" // Cyan
            7 -> "#0080FF" // Sky Blue
            8 -> "#0000FF" // Blue
            9 -> "#8000FF" // Purple
            else -> "#808080" // Gray
        }
    }
}

data class MemorizationGameState(
    val mode: MemorizationMode = MemorizationMode.PRACTICE,
    val startDigit: Int = 0,
    val targetDigits: String = "",
    val currentPosition: Int = 0,
    val correctAnswers: Int = 0,
    val totalAttempts: Int = 0,
    val isActive: Boolean = false,
    val isComplete: Boolean = false,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val lastAnswer: String = "",
    val lastAnswerCorrect: Boolean? = null
)