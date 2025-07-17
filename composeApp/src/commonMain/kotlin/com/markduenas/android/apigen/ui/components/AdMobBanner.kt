package com.markduenas.android.apigen.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.markduenas.android.apigen.ads.GlobalAdMobManager

/**
 * Cross-platform AdMob banner ad component
 * This is a wrapper around the new AdMobManager for backward compatibility
 */
@Composable
fun AdMobBanner(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    GlobalAdMobManager.BannerAdView(
        adUnitId = adUnitId,
        modifier = modifier
    )
}