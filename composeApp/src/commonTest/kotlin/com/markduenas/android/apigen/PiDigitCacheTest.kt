package com.markduenas.android.apigen

import com.markduenas.android.apigen.calculation.PiDigitCache
import com.markduenas.android.apigen.io.PiDataUnavailableException
import com.markduenas.android.apigen.io.PiDigitSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** Deterministic fake: digit at index i is i % 10. */
class FakeDigitSource(private val total: Int = 1_000_000) : PiDigitSource {
    override suspend fun initialize() {}
    override suspend fun readDigits(offset: Int, length: Int): String {
        val end = minOf(offset + length, total)
        return buildString { for (i in offset until end) append(i % 10) }
    }
    override fun getTotalDigits() = total
    override fun clearCache() {}
}

class PiDigitCacheTest {
    @Test
    fun requestLargerThanWindowReturnsEverything() = runTest {
        val cache = PiDigitCache(FakeDigitSource())
        val digits = cache.getDigits(0, 1_000_000)
        assertEquals(1_000_000, digits.length)
        assertEquals('9', digits.last())
    }

    @Test
    fun requestIsClampedToAvailableDigits() = runTest {
        val cache = PiDigitCache(FakeDigitSource(total = 25_000))
        assertEquals(25_000, cache.getDigits(0, 1_000_000).length)
        assertEquals(5_000, cache.getDigits(20_000, 50_000).length)
    }

    @Test
    fun requestStartingPastEndIsEmpty() = runTest {
        val cache = PiDigitCache(FakeDigitSource(total = 25_000))
        assertEquals("", cache.getDigits(30_000, 20_000))
    }

    @Test
    fun smallWindowedReadsStillWork() = runTest {
        val cache = PiDigitCache(FakeDigitSource())
        assertEquals("0123456789", cache.getDigits(0, 10))
        assertEquals("5678", cache.getDigits(500_005, 4))
        assertEquals("0123", cache.getDigits(0, 4)) // back to the start after window moved
    }

    @Test
    fun invalidArgumentsReturnEmpty() = runTest {
        val cache = PiDigitCache(FakeDigitSource())
        assertEquals("", cache.getDigits(-1, 10))
        assertEquals("", cache.getDigits(0, 0))
    }

    @Test
    fun initFailureSurfacesInsteadOfGeneratingDigits() = runTest {
        val source = object : PiDigitSource by FakeDigitSource() {
            override suspend fun initialize() = throw RuntimeException("no file")
        }
        assertFailsWith<PiDataUnavailableException> { PiDigitCache(source).getDigits(0, 100) }
    }

    @Test
    fun readFailureSurfacesInsteadOfGeneratingDigits() = runTest {
        val source = object : PiDigitSource by FakeDigitSource() {
            override suspend fun readDigits(offset: Int, length: Int): String = throw RuntimeException("io")
        }
        assertFailsWith<PiDataUnavailableException> { PiDigitCache(source).getDigits(0, 100) }
        assertFailsWith<PiDataUnavailableException> { PiDigitCache(source).getDigits(0, 50_000) }
    }

    @Test
    fun initIsRetriedAfterFailure() = runTest {
        var failures = 1
        val source = object : PiDigitSource by FakeDigitSource() {
            override suspend fun initialize() {
                if (failures-- > 0) throw RuntimeException("transient")
            }
        }
        val cache = PiDigitCache(source)
        assertFailsWith<PiDataUnavailableException> { cache.getDigits(0, 10) }
        assertEquals("0123456789", cache.getDigits(0, 10))
    }
}
