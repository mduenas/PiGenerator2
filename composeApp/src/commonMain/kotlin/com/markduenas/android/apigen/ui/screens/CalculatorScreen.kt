package com.markduenas.android.apigen.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.markduenas.android.apigen.calculation.PiCalculator
import com.markduenas.android.apigen.data.PiAlgorithm
import com.markduenas.android.apigen.data.PiCalculationProgress
import com.markduenas.android.apigen.data.PiCalculationResult
import com.markduenas.android.apigen.presentation.components.AdMobBanner
import com.markduenas.android.apigen.config.AdMobConstants
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    var selectedAlgorithm by remember { mutableStateOf(PiAlgorithm.MACHIN) }
    var digitCount by remember { mutableStateOf("100") }
    var calculationResult by remember { mutableStateOf<PiCalculationResult?>(null) }
    var isCalculating by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf<PiCalculationProgress?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var calculationJob by remember { mutableStateOf<Job?>(null) }
    
    val calculator = remember { PiCalculator() }
    val scope = rememberCoroutineScope()
    
    // Live calculation limit
    val maxLiveDigits = 10000
    
    // Clear results when algorithm changes
    LaunchedEffect(selectedAlgorithm) {
        calculationResult = null
        progress = null
        if (isCalculating) {
            calculationJob?.cancel()
            calculationJob = null
            isCalculating = false
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Banner Ad at the top
        AdMobBanner(AdMobConstants.getBannerAdUnitId())
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "\uD835\uDED1 Calculator",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    
                    // Algorithm Selection
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedAlgorithm.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Algorithm") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            PiAlgorithm.entries.forEach { algorithm ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(algorithm.displayName)
                                            Text(
                                                text = algorithm.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedAlgorithm = algorithm
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Digit Count Input
                    val digits = digitCount.toIntOrNull() ?: 100
                    val isValidInput = digits <= maxLiveDigits && digits > 0
                    
                    OutlinedTextField(
                        value = digitCount,
                        onValueChange = { if (it.all { char -> char.isDigit() }) digitCount = it },
                        label = { Text("Number of digits") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isValidInput && digitCount.isNotEmpty(),
                        supportingText = { 
                            if (!isValidInput && digitCount.isNotEmpty()) {
                                Text(
                                    "Maximum: $maxLiveDigits digits",
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text("Live calculation with real-time digit display")
                            }
                        }
                    )
                    
                    // Calculate Button
                    Button(
                        onClick = {
                            if (isValidInput && !isCalculating) {
                                // Live calculation mode with real-time display
                                isCalculating = true
                                calculationResult = null
                                progress = null
                                
                                calculationJob = scope.launch {
                                    calculator.calculatePi(
                                        digits = digits,
                                        algorithm = selectedAlgorithm,
                                        progressCallback = { prog -> progress = prog }
                                    ).collect { result ->
                                        calculationResult = result
                                        isCalculating = false
                                        calculationJob = null
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCalculating && isValidInput
                    ) {
                        if (isCalculating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isCalculating) "Calculating..." else "Calculate Pi")
                    }
                    
                    // Cancel Button (only show during calculations)
                    if (isCalculating) {
                        Button(
                            onClick = {
                                calculationJob?.cancel()
                                calculationJob = null
                                isCalculating = false
                                progress = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Cancel Calculation")
                        }
                    }
                }
            }
        }
        
        // Real-time Pi Digits Display (during calculation or completed)
        val displayContent = progress ?: calculationResult?.let { result ->
            // Create a "fake" progress object for completed calculations
            PiCalculationProgress(
                currentDigits = result.precision,
                targetDigits = result.precision,
                estimatedTimeRemainingMs = 0,
                currentResult = result.digits
            )
        }
        
        displayContent?.let { content ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (progress != null) "Calculating Pi" else "Pi Calculation Complete",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "${content.currentDigits}/${content.targetDigits}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        if (progress != null) {
                            LinearProgressIndicator(
                                progress = { content.currentDigits.toFloat() / content.targetDigits },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        // Real-time digits display with auto-scroll
                        SelectionContainer {
                            Text(
                                text = content.currentResult,
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.fillMaxWidth(),
                                lineHeight = 20.sp
                            )
                        }
                        
                        Text(
                            text = if (progress != null) {
                                "ETA: ${content.estimatedTimeRemainingMs / 1000}s • Algorithm: ${selectedAlgorithm.displayName}"
                            } else {
                                "Completed in ${calculationResult?.calculationTimeMs}ms • Algorithm: ${selectedAlgorithm.displayName}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Algorithm Information
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "About ${selectedAlgorithm.displayName}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = selectedAlgorithm.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    when (selectedAlgorithm) {
                        PiAlgorithm.MACHIN -> Text(
                            text = "Discovered by John Machin in 1706. Uses the formula: π/4 = 4*arctan(1/5) - arctan(1/239)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        PiAlgorithm.AGM_FFT -> Text(
                            text = "Uses Arithmetic-Geometric Mean with Fast Fourier Transform for maximum speed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        PiAlgorithm.SPIGOT -> Text(
                            text = "Memory-efficient algorithm that can calculate specific digit ranges without computing all preceding digits.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        }
    }
}