package com.markduenas.android.apigen.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Cross-platform interface for AdMob banner management
 */
expect class AdMobManager {
    
    /**
     * Creates and returns a platform-specific banner ad view
     * @param adUnitId The AdMob ad unit ID
     * @param modifier Compose modifier for styling
     */
    @Composable
    fun BannerAdView(
        adUnitId: String,
        modifier: Modifier
    )
}

/**
 * Get platform-specific AdMobManager instance
 */
expect fun getAdMobManager(): AdMobManager