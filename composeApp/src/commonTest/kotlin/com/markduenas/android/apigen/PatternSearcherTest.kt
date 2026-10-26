package com.markduenas.android.apigen

import com.markduenas.android.apigen.data.PatternType
import com.markduenas.android.apigen.patterns.PatternSearcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PatternSearcherTest {
    // 1,000,000 digits of "0" with markers planted past the old 10,000-digit limit.
    private val digits = CharArray(1_000_000) { '1' }.also {
        "98765".forEachIndexed { i, c -> it[500_000 + i] = c }
        "98765".forEachIndexed { i, c -> it[999_995 + i] = c }
    }.concatToString()

    private val searcher = PatternSearcher(digitProvider = { digits })

    @Test
    fun findsPatternsBeyondFirstTenThousandDigits() = runTest {
        val result = searcher.searchPattern("98765", PatternType.CUSTOM)
        assertEquals(listOf(500_000, 999_995), result.positions)
    }

    @Test
    fun overlappingMatchesAreAllReported() = runTest {
        val result = searcher.searchPattern("11", PatternType.CUSTOM)
        assertTrue(result.positions.size > 900_000)
    }

    @Test
    fun noMatchGivesEmptyPositions() = runTest {
        assertTrue(searcher.searchPattern("55555", PatternType.CUSTOM).positions.isEmpty())
    }

    @Test
    fun statisticsCoverAllDigits() = runTest {
        val stats = searcher.getPatternStatistics("98765")
        assertEquals(2, stats.occurrences)
        assertEquals(499_995.0, stats.averageDistance)
        assertEquals(2.0 / 1_000_000, stats.actualProbability)
    }

    @Test
    fun phoneNumberStripsFormatting() = runTest {
        val result = searcher.searchPhoneNumber("(98) 765")
        assertEquals(listOf(500_000, 999_995), result.positions)
    }
}
