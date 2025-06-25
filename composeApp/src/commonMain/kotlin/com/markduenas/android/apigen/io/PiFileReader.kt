package com.markduenas.android.apigen.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Cross-platform Pi file reader that efficiently reads chunks from the million digit file.
 * Uses platform-optimized implementations:
 * - Android/JVM: MappedByteBuffer for maximum performance
 * - iOS: Okio for cross-platform compatibility
 */
expect class PiFileReader() {
    /**
     * Read a chunk of digits from the file starting at the given offset
     */
    suspend fun readDigits(offset: Int, length: Int): String
    
    /**
     * Get the total number of digits available in the file
     */
    fun getTotalDigits(): Int
    
    /**
     * Initialize the file reader (platform-specific setup)
     */
    suspend fun initialize()
    
    /**
     * Clean up resources (if needed)
     */
    fun close()
}

/**
 * Simple LRU cache for caching file chunks
 */
class LRUCache<K, V>(private val maxSize: Int) {
    private val cache = mutableMapOf<K, V>()
    
    fun get(key: K): V? = cache[key]
    
    fun put(key: K, value: V) {
        if (cache.size >= maxSize) {
            val firstKey = cache.keys.first()
            cache.remove(firstKey)
        }
        cache[key] = value
    }
    
    fun contains(key: K): Boolean = cache.containsKey(key)
    
    fun clear() = cache.clear()
}

/**
 * Cached file reader that implements windowed reading with LRU cache
 */
class CachedPiFileReader {
    private val fileReader = PiFileReader()
    private val chunkCache = LRUCache<Int, String>(50) // Cache 50 chunks
    private var isInitialized = false
    
    companion object {
        private const val CHUNK_SIZE = 5000 // 5KB chunks for good balance of memory/performance
    }
    
    suspend fun initialize() {
        if (!isInitialized) {
            fileReader.initialize()
            isInitialized = true
        }
    }
    
    suspend fun readDigits(offset: Int, length: Int): String = withContext(Dispatchers.Default) {
        if (!isInitialized) initialize()
        
        val startChunk = offset / CHUNK_SIZE
        val endChunk = (offset + length - 1) / CHUNK_SIZE
        
        if (startChunk == endChunk) {
            // Single chunk read
            val chunk = getChunk(startChunk)
            val chunkOffset = offset % CHUNK_SIZE
            val endPos = (chunkOffset + length).coerceAtMost(chunk.length)
            chunk.substring(chunkOffset, endPos)
        } else {
            // Multi-chunk read
            val result = StringBuilder()
            for (chunkIndex in startChunk..endChunk) {
                val chunk = getChunk(chunkIndex)
                when (chunkIndex) {
                    startChunk -> {
                        val chunkOffset = offset % CHUNK_SIZE
                        result.append(chunk.substring(chunkOffset))
                    }
                    endChunk -> {
                        val endPos = ((offset + length) % CHUNK_SIZE).let { 
                            if (it == 0) CHUNK_SIZE else it 
                        }.coerceAtMost(chunk.length)
                        result.append(chunk.substring(0, endPos))
                    }
                    else -> {
                        result.append(chunk)
                    }
                }
            }
            result.toString()
        }
    }
    
    private suspend fun getChunk(chunkIndex: Int): String {
        return chunkCache.get(chunkIndex) ?: loadChunk(chunkIndex).also { 
            chunkCache.put(chunkIndex, it) 
        }
    }
    
    private suspend fun loadChunk(chunkIndex: Int): String {
        val offset = chunkIndex * CHUNK_SIZE
        val length = CHUNK_SIZE.coerceAtMost(getTotalDigits() - offset)
        return if (length > 0) {
            fileReader.readDigits(offset, length)
        } else {
            ""
        }
    }
    
    fun getTotalDigits(): Int = fileReader.getTotalDigits()
    
    fun clearCache() = chunkCache.clear()
    
    fun close() {
        fileReader.close()
        chunkCache.clear()
    }
    
    /**
     * Preload chunks around a center position for smooth scrolling
     */
    suspend fun preloadWindow(centerOffset: Int, windowSize: Int = 10000) {
        val startOffset = (centerOffset - windowSize / 2).coerceAtLeast(0)
        val endOffset = (centerOffset + windowSize / 2).coerceAtMost(getTotalDigits())
        
        val startChunk = startOffset / CHUNK_SIZE
        val endChunk = endOffset / CHUNK_SIZE
        
        for (chunkIndex in startChunk..endChunk) {
            if (!chunkCache.contains(chunkIndex)) {
                // Load chunk asynchronously without blocking
                try {
                    getChunk(chunkIndex)
                } catch (e: Exception) {
                    // Ignore preload errors
                }
            }
        }
    }
}