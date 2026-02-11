package com.markduenas.android.apigen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.markduenas.android.apigen.data.MemorizationMode
import com.markduenas.android.apigen.memorization.MemorizationGame
import com.markduenas.android.apigen.presentation.components.ConditionalAdBanner

@Composable
fun MemorizationScreen(modifier: Modifier = Modifier) {
    val game = remember { MemorizationGame() }
    val gameState by game.gameState.collectAsState()
    val stats by game.stats.collectAsState()
    var selectedMode by remember { mutableStateOf(MemorizationMode.PRACTICE) }
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Banner Ad at the top (hidden if user purchased ad removal)
        ConditionalAdBanner()
        
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
                        text = "\uD835\uDED1 Memorization Training",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    
                    // Mode Selection
                    Text(
                        text = "Training Mode",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(MemorizationMode.entries) { mode ->
                            FilterChip(
                                onClick = { selectedMode = mode },
                                label = { Text(mode.name.replace("_", " ")) },
                                selected = selectedMode == mode
                            )
                        }
                    }
                    
                    // Start Session Button
                    Button(
                        onClick = { 
                            coroutineScope.launch {
                                game.startSession(selectedMode)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !gameState.isActive
                    ) {
                        Text(if (gameState.isActive) "Session Active" else "Start Training")
                    }
                }
            }
        }
        
        // Game Session
        if (gameState.isActive || gameState.isComplete) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Position: ${gameState.currentPosition}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Accuracy: ${if (gameState.totalAttempts > 0) "${(gameState.correctAnswers * 100 / gameState.totalAttempts)}%" else "0%"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        LinearProgressIndicator(
                            progress = { if (gameState.targetDigits.isNotEmpty()) gameState.currentPosition.toFloat() / gameState.targetDigits.length else 0f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Pi Digits Display with Color Coding
                        if (gameState.targetDigits.isNotEmpty()) {
                            Text(
                                text = "π = 3.",
                                style = MaterialTheme.typography.titleLarge,
                                fontFamily = FontFamily.Monospace
                            )
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                items(gameState.targetDigits.length) { index ->
                                    val digit = gameState.targetDigits[index].digitToInt()
                                    val isRevealed = index < gameState.currentPosition
                                    val isCurrent = index == gameState.currentPosition && gameState.isActive
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isCurrent -> MaterialTheme.colorScheme.primary
                                                    isRevealed -> Color(game.getColorForDigit(digit).removePrefix("#").toLong(16) or 0xFF000000).copy(alpha = 0.3f)
                                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                                }
                                            )
                                            .border(
                                                width = if (isCurrent) 2.dp else 0.dp,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isRevealed || !gameState.isActive) digit.toString() else "?",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isCurrent -> MaterialTheme.colorScheme.onPrimary
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Input Section
                        if (gameState.isActive) {
                            Divider()
                            
                            Text(
                                text = "Enter the next digit:",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            // Number Pad
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (row in 0..2) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        for (col in 1..3) {
                                            val digit = row * 3 + col
                                            Button(
                                                onClick = { game.submitAnswer(digit.toString()) },
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(CircleShape)
                                            ) {
                                                Text(
                                                    text = digit.toString(),
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                            }
                                        }
                                    }
                                }
                                // Zero button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Button(
                                        onClick = { game.submitAnswer("0") },
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                    ) {
                                        Text(
                                            text = "0",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                            }
                            
                            // Feedback
                            gameState.lastAnswerCorrect?.let { correct ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (correct) 
                                            MaterialTheme.colorScheme.primaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Text(
                                        text = if (correct) "Correct! ✓" else "Try again! ✗",
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (correct) 
                                            MaterialTheme.colorScheme.onPrimaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                        
                        // Session Complete
                        if (gameState.isComplete) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Session Complete! 🎉",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Digits memorized: ${gameState.correctAnswers}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Accuracy: ${if (gameState.totalAttempts > 0) "${(gameState.correctAnswers * 100 / gameState.totalAttempts)}%" else "0%"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    
                                    Button(
                                        onClick = { game.resetGame() },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Start New Session")
                                    }
                                }
                            }
                        }
                        
                        // Control Buttons
                        if (gameState.isActive) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { game.endSession() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("End Session")
                                }
                                
                                if (selectedMode == MemorizationMode.PRACTICE) {
                                    Button(
                                        onClick = { 
                                            val hint = game.getHint(gameState.currentPosition)
                                            if (hint.isNotEmpty()) game.submitAnswer(hint)
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Hint")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Statistics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Your Statistics",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem("Personal Best", "${stats.personalBest} digits")
                        StatItem("Total Sessions", "${stats.totalSessions}")
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem("Total Digits", "${stats.totalDigitsMemorized}")
                        StatItem("Avg Accuracy", "${(stats.averageAccuracy * 100).toInt()}%")
                    }
                    
                    // Achievements
                    if (stats.achievementsUnlocked.isNotEmpty()) {
                        Text(
                            text = "Recent Achievements",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(stats.achievementsUnlocked.takeLast(3)) { achievement ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                    ),
                                    modifier = Modifier.width(120.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "🏆",
                                            style = MaterialTheme.typography.headlineSmall
                                        )
                                        Text(
                                            text = achievement.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}