package com.markduenas.android.apigen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.markduenas.android.apigen.billing.getBillingManager
import com.markduenas.android.apigen.ui.navigation.PiGeneratorNavHost

@Composable
@Preview
fun App() {
    // Initialize billing at app startup so ad removal state is loaded immediately
    LaunchedEffect(Unit) {
        getBillingManager().initialize()
    }

    PiGeneratorNavHost()
}