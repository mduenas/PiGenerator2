package com.markduenas.android.apigen.calculation

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.markduenas.android.apigen.io.CachedPiFileReader
import com.markduenas.android.apigen.io.PiDataUnavailableException
import com.markduenas.android.apigen.io.PiDigitSource
import kotlinx.coroutines.CancellationException
import kotlin.math.max
import kotlin.math.min

/**
 * A windowed cache for Pi digits that loads data from file on demand.
 * Maintains a sliding window of digits in memory for efficient scrolling.
 * Now uses the hybrid file reader for optimal performance across platforms.
 */
class PiDigitCache(private val fileReader: PiDigitSource = CachedPiFileReader()) {
    private val mutex = Mutex()
    private var cache: String = ""
    private var cacheStartIndex: Int = 0
    private val cacheSize = 10000 // Cache window size (about 10 screens worth)
    private var isInitialized = false
    
    // Initialize the file reader; a failure is retried on the next call
    private suspend fun ensureInitialized() {
        if (!isInitialized) {
            try {
                fileReader.initialize()
            } catch (e: CancellationException) {
                throw e
            } catch (e: PiDataUnavailableException) {
                throw e
            } catch (e: Exception) {
                throw PiDataUnavailableException("Failed to load pi digits", e)
            }
            isInitialized = true
        }
    }
    
    // Load digits from file. Never substitute generated digits: they would look like pi but be wrong.
    private suspend fun loadDigitsFromFile(startIndex: Int, length: Int): String {
        ensureInitialized()
        return try {
            fileReader.readDigits(startIndex, length)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw PiDataUnavailableException("Failed to read pi digits", e)
        }
    }
    
    /**
     * Get digits from the specified range, loading from file if needed
     */
    suspend fun getDigits(startIndex: Int, length: Int): String {
        if (startIndex < 0 || length <= 0) return ""
        mutex.withLock {
            // Requests bigger than the window (e.g. whole-file pattern search) can't be served
            // from it, so read them straight from the file reader.
            if (length > cacheSize) {
                ensureInitialized()
                val available = getAvailableDigitCount()
                if (startIndex >= available) return ""
                return loadDigitsFromFile(startIndex, min(length, available - startIndex))
            }

            val endIndex = startIndex + length
            
            // Check if we need to reload the cache
            if (cache.isEmpty() || 
                startIndex < cacheStartIndex || 
                endIndex > cacheStartIndex + cache.length) {
                
                // Calculate new cache window
                val newCacheStart = max(0, startIndex - cacheSize / 2)
                val totalDigits = getAvailableDigitCount()
                val newCacheEnd = min(totalDigits, newCacheStart + cacheSize)
                val newCacheLength = newCacheEnd - newCacheStart
                
                // Load new cache window
                cache = loadDigitsFromFile(newCacheStart, newCacheLength)
                cacheStartIndex = newCacheStart
            }
            
            // Extract requested digits from cache
            val localStart = max(0, startIndex - cacheStartIndex)
            val localEnd = min(cache.length, localStart + length)
            
            return if (localStart < cache.length && localEnd > localStart) {
                cache.substring(localStart, localEnd)
            } else {
                ""
            }
        }
    }
    
    /**
     * Get available digit count from file reader
     */
    fun getAvailableDigitCount(): Int {
        return if (isInitialized) {
            fileReader.getTotalDigits()
        } else {
            1000000 // Default assumption
        }
    }
    
    /**
     * Clear the cache to free memory
     */
    suspend fun clearCache() {
        mutex.withLock {
            cache = ""
            cacheStartIndex = 0
            fileReader.clearCache()
        }
    }
    
    /**
     * Preload a specific range into cache
     */
    suspend fun preloadRange(startIndex: Int, length: Int) {
        ensureInitialized()
        // Use file reader's optimized preloading
        fileReader.preloadWindow(startIndex, length)
        // Also trigger our local cache loading
        getDigits(startIndex, length)
    }
}

/**
 * Global instance of the Pi digit cache
 */
object PiDigitManager {
    private val cache = PiDigitCache()
    
    suspend fun getDigits(startIndex: Int, length: Int): String {
        return cache.getDigits(startIndex, length)
    }
    
    fun getAvailableDigitCount(): Int {
        return cache.getAvailableDigitCount()
    }
    
    suspend fun preloadRange(startIndex: Int, length: Int) {
        cache.preloadRange(startIndex, length)
    }
    
    suspend fun clearCache() {
        cache.clearCache()
    }
}