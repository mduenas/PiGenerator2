package com.markduenas.android.apigen.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.markduenas.android.apigen.calculation.PiDigitManager
import com.markduenas.android.apigen.ui.components.BannerAd
import kotlinx.coroutines.launch
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PiViewerScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    // State for windowed viewing
    var windowStartDigit by remember { mutableStateOf(0) }
    var cachedDigits by remember { mutableStateOf("") }
    var cacheStartIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Configuration
    val digitsPerLine = 50
    val linesPerPage = 20
    val digitsPerPage = digitsPerLine * linesPerPage
    val cacheSize = digitsPerPage * 5 // Cache 5 pages worth
    val totalAvailable = PiDigitManager.getAvailableDigitCount()
    
    // Calculate current viewing window
    val currentViewStartDigit = (listState.firstVisibleItemIndex * digitsPerLine).coerceAtLeast(0)
    val windowEndDigit = minOf(windowStartDigit + digitsPerPage, totalAvailable)
    
    // Load digits for current window
    LaunchedEffect(listState.firstVisibleItemIndex) {
        val neededStartDigit = currentViewStartDigit
        val neededEndDigit = minOf(neededStartDigit + digitsPerPage * 2, totalAvailable)
        
        // Check if we need to reload cache
        val cacheEndIndex = cacheStartIndex + cachedDigits.length
        if (neededStartDigit < cacheStartIndex || 
            neededEndDigit > cacheEndIndex ||
            cachedDigits.isEmpty()) {
            
            isLoading = true
            error = null
            
            try {
                // Load new cache window centered around current position
                val newCacheStart = maxOf(0, neededStartDigit - cacheSize / 2)
                val newCacheEnd = minOf(newCacheStart + cacheSize, totalAvailable)
                val newCacheLength = newCacheEnd - newCacheStart
                
                if (newCacheLength > 0) {
                    val newDigits = PiDigitManager.getDigits(newCacheStart, newCacheLength)
                    cachedDigits = newDigits
                    cacheStartIndex = newCacheStart
                    windowStartDigit = neededStartDigit
                }
                
            } catch (e: Exception) {
                error = "Failed to load digits: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    // Initial cache load
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val initialDigits = PiDigitManager.getDigits(0, cacheSize)
            cachedDigits = initialDigits
            cacheStartIndex = 0
            windowStartDigit = 0
        } catch (e: Exception) {
            error = "Failed to load initial digits: ${e.message}"
            cachedDigits = "141592653589793238462643383279502884197169399375105820974944592307816406286208998628034825342117067" // Fallback
            cacheStartIndex = 0
        } finally {
            isLoading = false
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Banner Ad at the top
        BannerAd()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "\uD835\uDED1 Digits Viewer",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Viewing: ${currentViewStartDigit + 1} - ${minOf(currentViewStartDigit + digitsPerPage, totalAvailable)} of $totalAvailable digits",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Loading more digits...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                error?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Pi digits display
        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            SelectionContainer {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Calculate total lines based on available digits
                    val totalLines = (totalAvailable + digitsPerLine - 1) / digitsPerLine
                    
                    // Generate all lines dynamically
                    items(totalLines) { lineIndex ->
                        val startDigitPos = lineIndex * digitsPerLine
                        val endDigitPos = minOf(startDigitPos + digitsPerLine, totalAvailable)
                        
                        // Get digits for this line from cache or show loading
                        val lineDigits = getLineDigits(
                            startDigitPos, 
                            endDigitPos, 
                            cacheStartIndex, 
                            cachedDigits,
                            isLoading
                        )
                        
                        PiDigitLine(
                            lineNumber = lineIndex,
                            digits = lineDigits,
                            digitsPerLine = digitsPerLine
                        )
                    }
                    
                    // Loading indicator at bottom
                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

/**
 * Get digits for a specific line from cache or return placeholder
 */
private fun getLineDigits(
    startDigitPos: Int,
    endDigitPos: Int, 
    cacheStartIndex: Int,
    cachedDigits: String,
    isLoading: Boolean
): String {
    return if (startDigitPos == 0) {
        // First line includes "3."
        val digitsNeeded = endDigitPos - 2 // Account for "3."
        if (digitsNeeded <= cachedDigits.length && cacheStartIndex == 0) {
            val digits = cachedDigits.substring(0, digitsNeeded)
            "$digits"
        } else {
            if (isLoading) "3.Loading..." else "3.${"•".repeat(47)}"
        }
    } else {
        // Regular lines - adjust for the fact that digit pos 0 is after "3."
        val adjustedStart = startDigitPos - 2
        val adjustedEnd = endDigitPos - 2
        
        if (adjustedStart >= 0 && 
            adjustedStart >= cacheStartIndex && 
            adjustedEnd <= cacheStartIndex + cachedDigits.length) {
            val cacheOffset = adjustedStart - cacheStartIndex
            val length = adjustedEnd - adjustedStart
            if (cacheOffset >= 0 && cacheOffset + length <= cachedDigits.length) {
                cachedDigits.substring(cacheOffset, cacheOffset + length)
            } else {
                if (isLoading) "Loading..." else "•".repeat(50)
            }
        } else {
            if (isLoading) "Loading..." else "•".repeat(50)
        }
    }
}

@Composable
private fun PiDigitLine(
    lineNumber: Int,
    digits: String,
    digitsPerLine: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Line number
        Text(
            text = "${lineNumber.toString().padStart(4, '0')}:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(50.dp),
            textAlign = TextAlign.End
        )
        
        // Digits
        Text(
            text = digits,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
        
        // Position indicator
        val startPosition = if (lineNumber == 0) 0 else lineNumber * digitsPerLine - 2
        Text(
            text = "${startPosition}+",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.End
        )
    }
}