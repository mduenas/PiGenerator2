package com.markduenas.android.apigen.data

data class MemorizationSession(
    val id: String,
    val startDigit: Int,
    val endDigit: Int,
    val correctAnswers: Int,
    val totalAttempts: Int,
    val timeSpentMs: Long,
    val date: Long,
    val mode: MemorizationMode
)

enum class MemorizationMode {
    PRACTICE,
    TEST,
    TIMED_CHALLENGE
}

data class MemorizationStats(
    val totalDigitsMemorized: Int,
    val personalBest: Int,
    val averageAccuracy: Float,
    val totalSessions: Int,
    val averageSessionTime: Long,
    val achievementsUnlocked: List<Achievement>
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val threshold: Int,
    val isUnlocked: Boolean,
    val unlockedDate: Long?
)

data class PatternSearchResult(
    val pattern: String,
    val positions: List<Int>,
    val patternType: PatternType
)

enum class PatternType {
    BIRTHDAY,
    PHONE_NUMBER,
    CUSTOM,
    FAMOUS_SEQUENCE
}