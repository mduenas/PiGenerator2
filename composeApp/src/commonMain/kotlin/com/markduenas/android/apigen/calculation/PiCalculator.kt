package com.markduenas.android.apigen.calculation

import com.markduenas.android.apigen.data.PiAlgorithm
import com.markduenas.android.apigen.data.PiCalculationProgress
import com.markduenas.android.apigen.data.PiCalculationResult
import com.markduenas.android.apigen.utils.getCurrentTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext
import kotlin.math.*

class PiCalculator {
    
    suspend fun calculatePi(
        digits: Int,
        algorithm: PiAlgorithm,
        progressCallback: ((PiCalculationProgress) -> Unit)? = null
    ): Flow<PiCalculationResult> = flow {
        val startTime = getCurrentTimeMillis()
        
        val result = when (algorithm) {
            PiAlgorithm.MACHIN -> calculateMachin(digits, progressCallback)
            PiAlgorithm.AGM_FFT -> calculateAGM(digits, progressCallback)
            PiAlgorithm.SPIGOT -> calculateSpigot(digits, progressCallback)
        }
        
        val endTime = getCurrentTimeMillis()
        
        emit(PiCalculationResult(
            digits = result,
            precision = digits,
            algorithm = algorithm,
            calculationTimeMs = endTime - startTime,
            isComplete = true
        ))
    }
    
    private suspend fun calculateMachin(
        digits: Int,
        progressCallback: ((PiCalculationProgress) -> Unit)?
    ): String {
        // Use windowed cache for digits
        val maxAvailableDigits = PiDigitManager.getAvailableDigitCount()
        val targetDigits = minOf(digits, maxAvailableDigits)
        
        // Start with "3." and add digits after decimal point
        val piBuilder = StringBuilder("3.")
        
        for (i in 1..targetDigits) {
            // Check for cancellation
            if (!coroutineContext.isActive) {
                break
            }
            
            delay(50) // Simulate calculation time
            
            // Get digit from windowed cache (skip "3." from file)
            val digit = PiDigitManager.getDigits(i + 1, 1) // Skip "3."
            if (digit.isNotEmpty()) {
                piBuilder.append(digit)
            }
            
            progressCallback?.invoke(
                PiCalculationProgress(
                    currentDigits = i,
                    targetDigits = targetDigits,
                    estimatedTimeRemainingMs = (targetDigits - i) * 50L,
                    currentResult = piBuilder.toString()
                )
            )
        }
        
        return piBuilder.toString()
    }
    
    private suspend fun calculateAGM(
        digits: Int,
        progressCallback: ((PiCalculationProgress) -> Unit)?
    ): String {
        // Use windowed cache for digits
        val maxAvailableDigits = PiDigitManager.getAvailableDigitCount()
        val targetDigits = minOf(digits, maxAvailableDigits)
        val piBuilder = StringBuilder("3.")
        
        // Simulate AGM calculation progress
        val chunkSize = maxOf(1, targetDigits / 20) // Show progress in chunks
        
        for (chunk in 0 until (targetDigits / chunkSize)) {
            // Check for cancellation
            if (!coroutineContext.isActive) {
                break
            }
            
            val startIdx = chunk * chunkSize + 1
            val endIdx = minOf(startIdx + chunkSize, targetDigits + 1)
            
            // Add digits from windowed cache (skip "3." from file)
            val chunkDigits = PiDigitManager.getDigits(startIdx + 1, endIdx - startIdx) // Skip "3."
            piBuilder.append(chunkDigits)
            
            delay(100) // Simulate calculation time
            
            progressCallback?.invoke(
                PiCalculationProgress(
                    currentDigits = endIdx - 1,
                    targetDigits = targetDigits,
                    estimatedTimeRemainingMs = ((targetDigits / chunkSize) - chunk - 1) * 100L,
                    currentResult = piBuilder.toString()
                )
            )
        }
        
        return piBuilder.toString()
    }
    
    private suspend fun calculateSpigot(
        digits: Int,
        progressCallback: ((PiCalculationProgress) -> Unit)?
    ): String {
        // Use windowed cache for digits
        val maxAvailableDigits = PiDigitManager.getAvailableDigitCount()
        val targetDigits = minOf(digits, maxAvailableDigits)
        val piBuilder = StringBuilder("3.")
        
        for (i in 1..targetDigits) {
            // Check for cancellation
            if (!coroutineContext.isActive) {
                break
            }
            
            delay(25) // Simulate calculation time
            
            // Get digit from windowed cache (skip "3." from file)
            val digit = PiDigitManager.getDigits(i + 1, 1) // Skip "3."
            if (digit.isNotEmpty()) {
                piBuilder.append(digit)
            }
            
            progressCallback?.invoke(
                PiCalculationProgress(
                    currentDigits = i,
                    targetDigits = targetDigits,
                    estimatedTimeRemainingMs = (targetDigits - i) * 25L,
                    currentResult = piBuilder.toString()
                )
            )
        }
        
        return piBuilder.toString()
    }
    
    companion object {
        suspend fun getDigitsString(startIndex: Int, length: Int): String {
            return PiDigitManager.getDigits(startIndex, length)
        }
        
        fun getAvailableDigitCount(): Int {
            return PiDigitManager.getAvailableDigitCount()
        }
    }
}