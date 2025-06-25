package com.markduenas.android.apigen.data

data class PiCalculationResult(
    val digits: String,
    val precision: Int,
    val algorithm: PiAlgorithm,
    val calculationTimeMs: Long,
    val isComplete: Boolean = true
)

enum class PiAlgorithm(val displayName: String, val description: String) {
    MACHIN("Machin's Formula", "Shows digits accumulating in real time"),
    AGM_FFT("AGM + FFT", "Fastest calculation method"),
    SPIGOT("Spigot Algorithm", "Memory-efficient for specific ranges")
}

data class PiCalculationProgress(
    val currentDigits: Int,
    val targetDigits: Int,
    val estimatedTimeRemainingMs: Long,
    val currentResult: String
)