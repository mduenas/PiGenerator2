package com.markduenas.android.apigen.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS implementation using Okio and platform APIs for file access
 */
actual class PiFileReader actual constructor() {
    private var totalDigits: Int = 0
    private var fileContent: String? = null
    
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun initialize() = withContext(Dispatchers.Default) {
        try {
            // Try multiple approaches to load the file on iOS
            val bundle = NSBundle.mainBundle
            
            // Try different possible locations/names for the file
            val possiblePaths = listOf(
                bundle.pathForResource("one_million", "txt"),
                bundle.pathForResource("one_million", null),
                bundle.pathForResource("one_million.txt", null),
                bundle.pathForResource("million_digits", "txt"),
                bundle.resourcePath?.let { "$it/one_million.txt" }
            )
            
            var loadedContent: String? = null
            
            for (path in possiblePaths) {
                if (path != null) {
                    try {
                        loadedContent = NSString.stringWithContentsOfFile(
                            path = path,
                            encoding = NSUTF8StringEncoding,
                            error = null
                        )
                        if (loadedContent != null && loadedContent.isNotEmpty()) {
                            break
                        }
                    } catch (e: Exception) {
                        // Continue to next path
                    }
                }
            }
            
            val content = loadedContent?.trimEnd()
            PiDataValidation.problemWith(content)?.let { throw PiDataUnavailableException(it) }
            fileContent = content
            totalDigits = content!!.length
        } catch (e: PiDataUnavailableException) {
            throw e
        } catch (e: Exception) {
            throw PiDataUnavailableException("Failed to load pi digits on iOS", e)
        }
    }

    actual suspend fun readDigits(offset: Int, length: Int): String = withContext(Dispatchers.Default) {
        val content = fileContent ?: throw IllegalStateException("File reader not initialized")
        
        if (offset < 0 || offset >= totalDigits) return@withContext ""
        val actualLength = length.coerceAtMost(totalDigits - offset)
        if (actualLength <= 0) return@withContext ""
        
        val endIndex = (offset + actualLength).coerceAtMost(content.length)
        content.substring(offset, endIndex)
    }
    
    actual fun getTotalDigits(): Int = totalDigits
    
    actual fun close() {
        // Release the file content from memory
        fileContent = null
        totalDigits = 0
    }
}