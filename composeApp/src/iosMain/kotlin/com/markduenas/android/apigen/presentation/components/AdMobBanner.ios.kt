package com.markduenas.android.apigen.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * iOS implementation of AdMob banner ad
 * Uses the new AdMobManager architecture
 */
@Composable
actual fun AdMobBanner(
    adUnitId: String,
    modifier: Modifier
) {
    val adMobManager = getAdMobManager()
    adMobManager.BannerAdView(
        adUnitId = adUnitId,
        modifier = modifier
    )
}
