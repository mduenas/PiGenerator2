package com.markduenas.android.apigen.io

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.RandomAccessFile
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.File
import java.io.FileOutputStream

/**
 * Android implementation using MappedByteBuffer for maximum performance
 */
actual class PiFileReader actual constructor() {
    private var mappedBuffer: MappedByteBuffer? = null
    private var channel: FileChannel? = null
    private var totalDigits: Int = 0
    private var tempFile: File? = null
    
    actual suspend fun initialize() = withContext(Dispatchers.Default) {
        try {
            // Get Android context - we'll need to pass this from the calling code
            val context = getAndroidContext()
            
            // Copy resource file to cache directory for memory mapping
            tempFile = File(context.cacheDir, "pi_digits.txt")
            
            if (!tempFile!!.exists()) {
                context.assets.open("one_million.txt").use { inputStream ->
                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
            
            // Memory map the file
            val randomAccessFile = RandomAccessFile(tempFile, "r")
            channel = randomAccessFile.channel
            mappedBuffer = channel!!.map(FileChannel.MapMode.READ_ONLY, 0, tempFile!!.length())
            totalDigits = tempFile!!.length().toInt()
            
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize Pi file reader on Android", e)
        }
    }
    
    actual suspend fun readDigits(offset: Int, length: Int): String = withContext(Dispatchers.Default) {
        val buffer = mappedBuffer ?: throw IllegalStateException("File reader not initialized")
        
        if (offset < 0 || offset >= totalDigits) return@withContext ""
        val actualLength = length.coerceAtMost(totalDigits - offset)
        if (actualLength <= 0) return@withContext ""
        
        val bytes = ByteArray(actualLength)
        synchronized(buffer) {
            buffer.position(offset)
            buffer.get(bytes, 0, actualLength)
        }
        String(bytes)
    }
    
    actual fun getTotalDigits(): Int = totalDigits
    
    actual fun close() {
        try {
            channel?.close()
            // Note: MappedByteBuffer cannot be explicitly unmapped in Android
            // It will be garbage collected eventually
            mappedBuffer = null
            channel = null
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
}

// We'll need to set this from MainActivity or App initialization
private var androidContext: Context? = null

fun setAndroidContext(context: Context) {
    androidContext = context
}

fun getAndroidContext(): Context {
    return androidContext ?: throw IllegalStateException("Android context not set. Call setAndroidContext() from MainActivity.")
}