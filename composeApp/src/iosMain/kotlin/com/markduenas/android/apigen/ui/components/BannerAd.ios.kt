package com.markduenas.android.apigen.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * iOS implementation of banner ad 
 * Currently shows placeholder until full iOS AdMob integration is implemented
 */
@Composable
actual fun BannerAd(
    modifier: Modifier
) {
    // For now, show placeholder on iOS
    // Full implementation would require GoogleMobileAds iOS SDK integration
    BannerAdPlaceholder(modifier = modifier)
}