package com.markduenas.android.apigen.ads

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.markduenas.android.apigen.config.AdMobConfig

class AndroidAdManager(private val context: Context) : AdManager {
    
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    
    override fun initializeAds() {
        MobileAds.initialize(context)
        
        if (AdMobConfig.TEST_MODE) {
            val testDeviceIds = listOf(AdRequest.DEVICE_ID_EMULATOR)
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(configuration)
        }
    }
    
    override fun loadBannerAd(adUnitId: String) {
        // Banner ads are handled in Composables
    }
    
    override fun showInterstitialAd(adUnitId: String, callback: (Boolean) -> Unit) {
        if (interstitialAd != null && context is Activity) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd(adUnitId)
                    callback(true)
                }
                
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    callback(false)
                }
            }
            interstitialAd?.show(context)
        } else {
            loadInterstitialAd(adUnitId)
            callback(false)
        }
    }
    
    override fun showRewardedAd(adUnitId: String, callback: (Boolean) -> Unit) {
        if (rewardedAd != null && context is Activity) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd(adUnitId)
                }
                
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    callback(false)
                }
            }
            
            rewardedAd?.show(context) { rewardItem ->
                callback(true)
            }
        } else {
            loadRewardedAd(adUnitId)
            callback(false)
        }
    }
    
    override fun isAdLoaded(adType: AdType): Boolean {
        return when (adType) {
            AdType.INTERSTITIAL -> interstitialAd != null
            AdType.REWARDED -> rewardedAd != null
            AdType.BANNER -> true // Banner ads are always "loaded" in compose
        }
    }
    
    private fun loadInterstitialAd(adUnitId: String) {
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
            }
            
            override fun onAdFailedToLoad(adError: LoadAdError) {
                interstitialAd = null
            }
        })
    }
    
    private fun loadRewardedAd(adUnitId: String) {
        val adRequest = AdRequest.Builder().build()
        
        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
            }
            
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
            }
        })
    }
    
    init {
        // Pre-load ads
        loadInterstitialAd(AdMobConfig.ANDROID_INTERSTITIAL_AD_UNIT_ID)
        loadRewardedAd(AdMobConfig.ANDROID_REWARDED_AD_UNIT_ID)
    }
}

@Composable
actual fun createPlatformAdManager(): AdManager {
    val context = LocalContext.current
    return remember { AndroidAdManager(context) }
}

@Composable
fun BannerAdView(
    adUnitId: String = AdMobConfig.ANDROID_BANNER_AD_UNIT_ID,
    adSize: AdSize = AdSize.BANNER
) {
    val context = LocalContext.current
    
    AndroidView(
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(adSize)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { adView ->
            adView.loadAd(AdRequest.Builder().build())
        }
    )
}