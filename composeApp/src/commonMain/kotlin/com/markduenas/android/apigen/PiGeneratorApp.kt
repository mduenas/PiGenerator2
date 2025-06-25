package com.markduenas.android.apigen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.markduenas.android.apigen.ui.navigation.PiGeneratorNavHost
import com.markduenas.android.apigen.ui.theme.PiGeneratorTheme

@Composable
fun PiGeneratorApp() {
    PiGeneratorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PiGeneratorNavHost()
        }
    }
}