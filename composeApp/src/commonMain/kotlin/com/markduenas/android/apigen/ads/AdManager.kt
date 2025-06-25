package com.markduenas.android.apigen.ads

import androidx.compose.runtime.Composable

interface AdManager {
    fun initializeAds()
    fun loadBannerAd(adUnitId: String)
    fun showInterstitialAd(adUnitId: String, callback: (Boolean) -> Unit)
    fun showRewardedAd(adUnitId: String, callback: (Boolean) -> Unit)
    fun isAdLoaded(adType: AdType): Boolean
}

enum class AdType {
    BANNER,
    INTERSTITIAL,
    REWARDED
}

@Composable
expect fun createPlatformAdManager(): AdManager