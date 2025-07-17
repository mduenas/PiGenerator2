package com.markduenas.android.apigen.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.markduenas.android.apigen.data.PatternSearchResult
import com.markduenas.android.apigen.data.PatternType
import com.markduenas.android.apigen.patterns.PatternSearcher
import com.markduenas.android.apigen.utils.formatString
import com.markduenas.android.apigen.patterns.PatternStatistics
import com.markduenas.android.apigen.ui.components.AdMobBanner
import com.markduenas.android.apigen.data.admob.AdMobConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternExplorerScreen(modifier: Modifier = Modifier) {
    val patternSearcher = remember { PatternSearcher() }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<PatternSearchResult>>(emptyList()) }
    var patternStats by remember { mutableStateOf<PatternStatistics?>(null) }
    var selectedPatternType by remember { mutableStateOf(PatternType.CUSTOM) }
    var isSearching by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // Birthday search fields
    var birthMonth by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }
    var birthYear by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        // Load famous sequences on screen load
        searchResults = patternSearcher.getFamousSequences()
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
                        text = "\uD835\uDED1 Pattern Explorer",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    
                    Text(
                        text = "Search for patterns in the first million digits of π",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Pattern Type Selection
                    Text(
                        text = "Search Type",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { selectedPatternType = PatternType.CUSTOM },
                            label = { Text("Custom") },
                            selected = selectedPatternType == PatternType.CUSTOM,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            onClick = { selectedPatternType = PatternType.BIRTHDAY },
                            label = { Text("Birthday") },
                            selected = selectedPatternType == PatternType.BIRTHDAY,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            onClick = { selectedPatternType = PatternType.PHONE_NUMBER },
                            label = { Text("Phone") },
                            selected = selectedPatternType == PatternType.PHONE_NUMBER,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Search Input based on type
                    when (selectedPatternType) {
                        PatternType.CUSTOM, PatternType.PHONE_NUMBER -> {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it.filter { char -> char.isDigit() } },
                                label = { 
                                    Text(
                                        if (selectedPatternType == PatternType.PHONE_NUMBER) 
                                            "Phone Number" 
                                        else 
                                            "Pattern (digits only)"
                                    ) 
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (searchQuery.isNotEmpty()) {
                                                coroutineScope.launch {
                                                    isSearching = true
                                                    val result = if (selectedPatternType == PatternType.PHONE_NUMBER) {
                                                        patternSearcher.searchPhoneNumber(searchQuery)
                                                    } else {
                                                        patternSearcher.searchPattern(searchQuery, PatternType.CUSTOM)
                                                    }
                                                    searchResults = listOf(result)
                                                    patternStats = patternSearcher.getPatternStatistics(searchQuery)
                                                    isSearching = false
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Search, contentDescription = "Search")
                                    }
                                }
                            )
                        }
                        
                        PatternType.BIRTHDAY -> {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = birthMonth,
                                        onValueChange = { if (it.length <= 2 && it.all { char -> char.isDigit() }) birthMonth = it },
                                        label = { Text("Month") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        placeholder = { Text("MM") }
                                    )
                                    OutlinedTextField(
                                        value = birthDay,
                                        onValueChange = { if (it.length <= 2 && it.all { char -> char.isDigit() }) birthDay = it },
                                        label = { Text("Day") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        placeholder = { Text("DD") }
                                    )
                                    OutlinedTextField(
                                        value = birthYear,
                                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) birthYear = it },
                                        label = { Text("Year") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        placeholder = { Text("YYYY") }
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        val month = birthMonth.toIntOrNull()
                                        val day = birthDay.toIntOrNull()
                                        val year = birthYear.toIntOrNull()
                                        
                                        if (month != null && day != null && year != null) {
                                            coroutineScope.launch {
                                                isSearching = true
                                                val result = patternSearcher.searchBirthday(month, day, year)
                                                searchResults = listOf(result)
                                                patternStats = patternSearcher.getPatternStatistics(result.pattern)
                                                isSearching = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = birthMonth.isNotEmpty() && birthDay.isNotEmpty() && birthYear.isNotEmpty()
                                ) {
                                    if (isSearching) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text("Search Birthday")
                                }
                            }
                        }
                        
                        PatternType.FAMOUS_SEQUENCE -> {
                            // This is handled by the initial load
                        }
                    }
                    
                    // Quick Actions
                    Text(
                        text = "Quick Searches",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    searchResults = patternSearcher.getFamousSequences()
                                    patternStats = null
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Famous Patterns")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val result = patternSearcher.searchPattern("123456", PatternType.CUSTOM)
                                    searchResults = listOf(result)
                                    patternStats = patternSearcher.getPatternStatistics("123456")
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sequential")
                        }
                    }
                }
            }
        }
        
        // Pattern Statistics
        patternStats?.let { stats ->
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
                            text = "Pattern Statistics",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatisticItem("Occurrences", "${stats.occurrences}")
                            StatisticItem("Avg Distance", "${stats.averageDistance.toInt()}")
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatisticItem("Theoretical Prob", formatString("%.2e", stats.theoreticalProbability))
                            StatisticItem("Actual Prob", formatString("%.2e", stats.actualProbability))
                        }
                    }
                }
            }
        }
        
        // Search Results
        if (searchResults.isNotEmpty()) {
            items(searchResults) { result ->
                SearchResultCard(result = result)
            }
        }
        
        // Information Card
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
                        text = "About Pattern Search",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = "• Find your birthday, phone number, or any sequence in π",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Explore famous mathematical sequences",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Learn about probability and randomness in π",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Discover the Feynman Point and other curiosities",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun SearchResultCard(result: PatternSearchResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    text = "Pattern: ${result.pattern}",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace
                )
                
                Text(
                    text = "${result.positions.size} found",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (result.positions.isNotEmpty()) {
                Text(
                    text = "Found at positions:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Show first few positions
                val displayPositions = result.positions.take(10)
                Text(
                    text = displayPositions.joinToString(", ") + 
                          if (result.positions.size > 10) "... and ${result.positions.size - 10} more" else "",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Special information for famous sequences
                when (result.pattern) {
                    "999999" -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = "🎯 Feynman Point: Six consecutive 9s starting at position ${result.positions.firstOrNull() ?: "unknown"}",
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    "314159" -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                text = "📐 The beginning of π appears again later in its own digits!",
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Pattern not found in the available digits of π",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}