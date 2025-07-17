package com.markduenas.android.apigen.ads

import androidx.compose.runtime.Composable
import com.markduenas.android.apigen.config.AdMobConfig
import kotlinx.cinterop.ExperimentalForeignApi

class IOSAdManager : AdManager {
    
    override fun initializeAds() {
        // AdMob initialization is handled in the iOS app delegate via iOSApp.swift
        // The Swift bridge provides additional initialization if needed
        println("AdMob iOS SDK initialized via iOSApp.swift")
    }
    
    override fun loadBannerAd(adUnitId: String) {
        println("Loading iOS banner ad: $adUnitId")
        // Banner ad loading is handled automatically by the BannerAdView
    }
    
    override fun showInterstitialAd(adUnitId: String, callback: (Boolean) -> Unit) {
        println("Interstitial ads not implemented for this app")
        // Not implementing interstitial ads as requested
        callback(false)
    }
    
    override fun showRewardedAd(adUnitId: String, callback: (Boolean) -> Unit) {
        println("Rewarded ads not implemented for this app")
        // Not implementing rewarded ads as requested
        callback(false)
    }
    
    override fun isAdLoaded(adType: AdType): Boolean {
        return when (adType) {
            AdType.BANNER -> true // Banner ads are loaded automatically
            else -> false // Other ad types not implemented
        }
    }
}

@Composable
actual fun createPlatformAdManager(): AdManager {
    return IOSAdManager()
}