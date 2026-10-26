package com.markduenas.android.apigen

import com.markduenas.android.apigen.io.PiDataValidation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PiDataValidationTest {
    private val full = "3." + "1".repeat(1_000_000)

    @Test
    fun completeFileIsValid() {
        assertNull(PiDataValidation.problemWith(full))
    }

    @Test
    fun expectedLengthMatchesBundledFile() {
        assertEquals(1_000_002, full.length)
    }

    @Test
    fun missingOrEmptyIsRejected() {
        assertNotNull(PiDataValidation.problemWith(null))
        assertNotNull(PiDataValidation.problemWith(""))
    }

    @Test
    fun truncatedFileIsRejected() {
        assertNotNull(PiDataValidation.problemWith(full.take(100_000)))
    }

    @Test
    fun wrongPrefixIsRejected() {
        assertNotNull(PiDataValidation.problemWith("4." + "1".repeat(1_000_000)))
    }

    @Test
    fun nonDigitsAreRejected() {
        assertNotNull(PiDataValidation.problemWith("3." + "1".repeat(500_000) + "x" + "1".repeat(499_999)))
    }
}
