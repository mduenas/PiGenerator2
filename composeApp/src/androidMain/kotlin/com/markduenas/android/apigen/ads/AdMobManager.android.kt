package com.markduenas.android.apigen.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import android.content.Context
import com.markduenas.android.apigen.config.BuildConfig
import com.markduenas.android.apigen.data.admob.AdMobConstants

/**
 * Android implementation of AdMobManager
 * Uses Google Mobile Ads SDK for Android
 */
actual class AdMobManager {
    
    private var initialized = false
    
    @Composable
    actual fun BannerAdView(
        adUnitId: String,
        modifier: Modifier
    ) {
        val context = LocalContext.current
        
        // Initialize AdMob if not already done
        if (!initialized) {
            initializeMobileAds(context)
            initialized = true
        }
        
        // Always show real ads with proper logging
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(AdMobConstants.BANNER_HEIGHT_DP.dp),
            factory = { ctx ->
                println("AdMob: Creating AdView with adUnitId: $adUnitId")
                println("AdMob: Debug mode: ${BuildConfig.isDebug}")
                
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    setAdUnitId(adUnitId)
                    
                    // Add ad listener for debugging
                    adListener = object : com.google.android.gms.ads.AdListener() {
                        override fun onAdLoaded() {
                            println("AdMob: Banner ad loaded successfully")
                        }
                        
                        override fun onAdFailedToLoad(adError: com.google.android.gms.ads.LoadAdError) {
                            println("AdMob: Banner ad failed to load: ${adError.message}")
                            println("AdMob: Error code: ${adError.code}")
                            println("AdMob: Error domain: ${adError.domain}")
                            println("AdMob: Error cause: ${adError.cause}")
                        }
                        
                        override fun onAdOpened() {
                            println("AdMob: Banner ad opened")
                        }
                        
                        override fun onAdClosed() {
                            println("AdMob: Banner ad closed")
                        }
                        
                        override fun onAdImpression() {
                            println("AdMob: Banner ad impression recorded")
                        }
                    }
                    
                    println("AdMob: Loading ad request...")
                    loadAd(AdRequest.Builder().build())
                }
            },
            update = { adView ->
                // Ensure the ad view is properly configured on updates
                if (adView.adUnitId != adUnitId) {
                    println("AdMob: Updating AdView with new adUnitId: $adUnitId")
                    adView.setAdUnitId(adUnitId)
                    adView.loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
    
    actual fun initialize() {
        if (!initialized) {
            // AdMob initialization happens in MainActivity's AndroidAdManager
            // This is just marking that we've attempted initialization
            initialized = true
        }
    }
    
    actual fun isReady(): Boolean {
        return initialized
    }
    
    private fun initializeMobileAds(context: Context) {
        println("AdMob: Initializing MobileAds...")
        
        MobileAds.initialize(context) { initializationStatus ->
            println("AdMob: MobileAds initialization completed")
            println("AdMob: Adapter status map: ${initializationStatus.adapterStatusMap}")
            
            for ((adapterName, status) in initializationStatus.adapterStatusMap) {
                println("AdMob: Adapter $adapterName - Status: ${status.initializationState}, Description: ${status.description}")
            }
        }
        
        // Set test device configuration if in debug mode
        if (BuildConfig.isDebug) {
            println("AdMob: Setting test device configuration for debug mode")
            val testDeviceIds = listOf(AdRequest.DEVICE_ID_EMULATOR)
            val configuration = com.google.android.gms.ads.RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(configuration)
        }
    }
}

/**
 * Global Android AdMob manager instance
 */
actual val GlobalAdMobManager: AdMobManager = AdMobManager().also {
    println("AdMob: Global Android AdMobManager instance created")
}