package com.markduenas.android.apigen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.markduenas.android.apigen.io.setAndroidContext
import com.markduenas.android.apigen.config.setAndroidContext as setAdMobContext
import com.markduenas.android.apigen.billing.getBillingManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Set Android context for file reader
        setAndroidContext(this)

        // Set Android context for AdMob
        com.markduenas.android.apigen.config.setAndroidContext(this)

        // Set activity for billing manager (required for purchase flow)
        getBillingManager().setActivity(this)

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up billing client
        getBillingManager().cleanup()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}