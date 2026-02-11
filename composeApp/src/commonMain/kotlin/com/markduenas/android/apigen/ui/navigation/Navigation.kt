package com.markduenas.android.apigen.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.markduenas.android.apigen.ui.screens.CalculatorScreen
import com.markduenas.android.apigen.ui.screens.MemorizationScreen
import com.markduenas.android.apigen.ui.screens.PatternExplorerScreen
import com.markduenas.android.apigen.ui.screens.PiViewerScreen
import com.markduenas.android.apigen.ui.screens.SettingsScreen
import com.markduenas.android.apigen.ui.theme.PiGeneratorTheme

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Calculator : Screen("calculator", "Calculator", Icons.Default.Calculate)
    object Memorization : Screen("memorization", "Training", Icons.Default.Psychology)
    object PatternExplorer : Screen("patterns", "Explorer", Icons.Default.Search)
    object PiViewer : Screen("viewer", "Viewer", Icons.Default.Visibility)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PiGeneratorNavHost() {
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Calculator) }

    PiGeneratorTheme {
        Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pi Generator") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(
                        onClick = { selectedScreen = Screen.Settings }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val screens = listOf(Screen.Calculator, Screen.Memorization, Screen.PatternExplorer, Screen.PiViewer)
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selectedScreen == screen,
                        onClick = { selectedScreen = screen }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedScreen) {
                Screen.Calculator -> CalculatorScreen()
                Screen.Memorization -> MemorizationScreen()
                Screen.PatternExplorer -> PatternExplorerScreen()
                Screen.PiViewer -> PiViewerScreen()
                Screen.Settings -> SettingsScreen()
            }
        }
    }
    }
}