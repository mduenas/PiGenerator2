package com.markduenas.android.apigen.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * AdMob Manager for cross-platform ad integration
 * Provides a unified interface for displaying AdMob ads across Android and iOS
 */
expect class AdMobManager {
    
    /**
     * Display a banner ad
     * @param adUnitId The ad unit ID to use
     * @param modifier Compose modifier for styling
     */
    @Composable
    fun BannerAdView(
        adUnitId: String,
        modifier: Modifier = Modifier
    )
    
    /**
     * Initialize AdMob SDK if needed
     */
    fun initialize()
    
    /**
     * Check if AdMob is ready to display ads
     */
    fun isReady(): Boolean
}

/**
 * Global AdMob manager instance
 */
expect val GlobalAdMobManager: AdMobManager