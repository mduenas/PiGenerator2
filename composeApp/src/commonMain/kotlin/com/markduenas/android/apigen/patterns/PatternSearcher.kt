package com.markduenas.android.apigen.patterns

import com.markduenas.android.apigen.calculation.PiDigitManager
import com.markduenas.android.apigen.data.PatternSearchResult
import com.markduenas.android.apigen.data.PatternType
import com.markduenas.android.apigen.utils.formatString

class PatternSearcher {
    
    suspend fun searchPattern(pattern: String, patternType: PatternType): PatternSearchResult {
        val piDigits = PiDigitManager.getDigits(0, 1000000) // Use full million digits for pattern search from file
        val positions = findAllOccurrences(piDigits, pattern)
        
        return PatternSearchResult(
            pattern = pattern,
            positions = positions,
            patternType = patternType
        )
    }
    
    suspend fun searchBirthday(month: Int, day: Int, year: Int): PatternSearchResult {
        val patterns = listOf(
            formatString("%02d%02d%04d", month, day, year),
            formatString("%02d%02d%02d", month, day, year % 100),
            formatString("%d%d%d", month, day, year),
            formatString("%d%d%d", month, day, year % 100)
        )
        
        var bestResult: PatternSearchResult? = null
        
        patterns.forEach { pattern ->
            val result = searchPattern(pattern, PatternType.BIRTHDAY)
            if (bestResult == null || result.positions.isNotEmpty()) {
                bestResult = result
            }
        }
        
        return bestResult ?: PatternSearchResult("", emptyList(), PatternType.BIRTHDAY)
    }
    
    suspend fun searchPhoneNumber(phoneNumber: String): PatternSearchResult {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9]"), "")
        return searchPattern(cleanNumber, PatternType.PHONE_NUMBER)
    }
    
    suspend fun getFamousSequences(): List<PatternSearchResult> {
        val famousPatterns = mapOf(
            "999999" to "Feynman Point",
            "123456" to "Sequential Digits",
            "314159" to "Pi Beginning",
            "271828" to "Euler's Number Beginning",
            "161803" to "Golden Ratio Beginning",
            "000000" to "Six Zeros"
        )
        
        return famousPatterns.map { (pattern, _) ->
            searchPattern(pattern, PatternType.FAMOUS_SEQUENCE)
        }
    }
    
    private fun findAllOccurrences(text: String, pattern: String): List<Int> {
        val positions = mutableListOf<Int>()
        var startIndex = 0
        
        while (startIndex < text.length) {
            val index = text.indexOf(pattern, startIndex)
            if (index != -1) {
                positions.add(index)
                startIndex = index + 1
            } else {
                break
            }
        }
        
        return positions
    }
    
    suspend fun getPatternStatistics(pattern: String): PatternStatistics {
        val piDigits = PiDigitManager.getDigits(0, 1000000)
        val positions = findAllOccurrences(piDigits, pattern)
        
        val distances = if (positions.size > 1) {
            positions.zipWithNext { a, b -> b - a }
        } else emptyList()
        
        val averageDistance = if (distances.isNotEmpty()) {
            distances.average()
        } else 0.0
        
        // Calculate probability (very simplified)
        val probability = if (pattern.isNotEmpty()) {
            1.0 / (10.0.pow(pattern.length))
        } else 0.0
        
        return PatternStatistics(
            occurrences = positions.size,
            positions = positions,
            averageDistance = averageDistance,
            theoreticalProbability = probability,
            actualProbability = positions.size.toDouble() / piDigits.length
        )
    }
    
    private fun Double.pow(n: Int): Double {
        var result = 1.0
        repeat(n) { result *= this }
        return result
    }
}

data class PatternStatistics(
    val occurrences: Int,
    val positions: List<Int>,
    val averageDistance: Double,
    val theoreticalProbability: Double,
    val actualProbability: Double
)