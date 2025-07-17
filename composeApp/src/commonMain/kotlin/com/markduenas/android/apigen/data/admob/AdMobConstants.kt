package com.markduenas.android.apigen.data.admob

import com.markduenas.android.apigen.getPlatform
import com.markduenas.android.apigen.config.AdMobConfig

/**
 * AdMob configuration constants
 */
object AdMobConstants {
    
    /**
     * Banner ad height in dp
     */
    const val BANNER_HEIGHT_DP = 50
    
    /**
     * Whether AdMob is enabled (can be controlled by feature flags)
     */
    const val ADMOB_ENABLED = true
    
    /**
     * Get the platform-specific banner ad unit ID
     */
    fun getBannerAdUnitId(): String {
        val platform = getPlatform()
        val adUnitId = if (platform.name.startsWith("Android")) {
            AdMobConfig.ANDROID_BANNER_AD_UNIT_ID
        } else {
            AdMobConfig.IOS_BANNER_AD_UNIT_ID
        }
        println("AdMob: Using ad unit ID: $adUnitId for platform: ${platform.name}")
        return adUnitId
    }
}