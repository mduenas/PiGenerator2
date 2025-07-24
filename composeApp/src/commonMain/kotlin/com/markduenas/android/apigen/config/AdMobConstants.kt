package com.markduenas.android.apigen.config

import com.markduenas.android.apigen.getPlatform

/**
 * AdMob configuration constants
 */
object AdMobConstants {
    
    /**
     * Android AdMob Banner Ad Unit ID
     * For testing, use: "ca-app-pub-3940256099942544/6300978111"
     * Replace with your actual ad unit ID for production
     * ca-app-pub-7540731406850248/9946550555
     */
    const val ANDROID_BANNER_AD_UNIT_ID = "ca-app-pub-7540731406850248/9946550555"
    
    /**
     * iOS AdMob Banner Ad Unit ID  
     * For testing, use: "ca-app-pub-3940256099942544/2934735716"
     * Replace with your actual ad unit ID for production
     */
    const val IOS_BANNER_AD_UNIT_ID = "ca-app-pub-7540731406850248/7811996861"
    
    /**
     * AdMob Application IDs
     */
    const val ANDROID_APPLICATION_ID = "ca-app-pub-7540731406850248~8469817354"
    const val IOS_APPLICATION_ID = "ca-app-pub-7540731406850248~1629731896"
    
    /**
     * Banner ad height in dp
     */
    const val BANNER_HEIGHT_DP = 50
    
    /**
     * Whether AdMob is enabled (can be controlled by feature flags)
     */
    const val ADMOB_ENABLED = true

    /**
     * Automatically detects test mode based on build type
     * Returns true for debug builds, false for release builds
     */
    val TEST_MODE: Boolean
        get() = BuildConfig.isDebug
    
    /**
     * Get the platform-specific banner ad unit ID
     */
    fun getBannerAdUnitId(): String {
        val platform = getPlatform()
        return if (platform.name.startsWith("Android")) {
            ANDROID_BANNER_AD_UNIT_ID
        } else {
            IOS_BANNER_AD_UNIT_ID
        }
    }

}