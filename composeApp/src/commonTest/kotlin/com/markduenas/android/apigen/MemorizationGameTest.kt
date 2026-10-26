package com.markduenas.android.apigen

import com.markduenas.android.apigen.data.MemorizationMode
import com.markduenas.android.apigen.io.PiDataUnavailableException
import com.markduenas.android.apigen.memorization.MemorizationGame
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MemorizationGameTest {
    private fun game(available: Int = 1_000_000) = MemorizationGame(
        digitProvider = { start, length ->
            buildString { for (i in start until minOf(start + length, available)) append(i % 10) }
        },
        availableDigits = { available }
    )

    @Test
    fun defaultsTo100Digits() = runTest {
        val g = game()
        g.startSession(MemorizationMode.PRACTICE)
        assertEquals(100, g.gameState.value.targetDigits.length)
    }

    @Test
    fun supportsSessionsLongerThan100Digits() = runTest {
        val g = game()
        for (count in MemorizationGame.DIGIT_COUNT_OPTIONS) {
            g.resetGame()
            g.startSession(MemorizationMode.PRACTICE, digitCount = count)
            assertEquals(count, g.gameState.value.targetDigits.length)
        }
    }

    @Test
    fun digitCountIsClampedToAvailableDigits() = runTest {
        val g = game(available = 300)
        g.startSession(MemorizationMode.PRACTICE, startDigit = 100, digitCount = 1000)
        assertEquals(200, g.gameState.value.targetDigits.length)
    }

    @Test
    fun nonPositiveDigitCountIsClampedToOne() = runTest {
        val g = game()
        g.startSession(MemorizationMode.PRACTICE, digitCount = 0)
        assertEquals(1, g.gameState.value.targetDigits.length)
    }

    @Test
    fun sessionRunsPast100DigitsAndCompletes() = runTest {
        val g = game()
        g.startSession(MemorizationMode.PRACTICE, digitCount = 250)
        repeat(250) { i -> assertTrue(g.submitAnswer((i % 10).toString())) }
        val state = g.gameState.value
        assertTrue(state.isComplete)
        assertFalse(state.isActive)
        assertEquals(250, state.correctAnswers)
        assertEquals(250, g.stats.value.personalBest)
        assertTrue(g.stats.value.achievementsUnlocked.any { it.threshold == 250 })
    }

    @Test
    fun testModeEndsOnFirstMistake() = runTest {
        val g = game()
        g.startSession(MemorizationMode.TEST, digitCount = 500)
        assertFalse(g.submitAnswer("9"))
        assertTrue(g.gameState.value.isComplete)
    }

    @Test
    fun missingPiDataReportsErrorInsteadOfCrashing() = runTest {
        val g = MemorizationGame(
            digitProvider = { _, _ -> throw PiDataUnavailableException("gone") },
            availableDigits = { 1_000_000 }
        )
        g.startSession(MemorizationMode.PRACTICE)
        assertNotNull(g.gameState.value.errorMessage)
        assertFalse(g.gameState.value.isActive)
        assertEquals("", g.gameState.value.targetDigits)
    }
}
