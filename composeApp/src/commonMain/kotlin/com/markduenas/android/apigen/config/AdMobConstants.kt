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
     * True for debug, TestFlight, Play-internal (USE_TEST_ADS), and sideloaded builds.
     * False only for production Play / App Store distribution.
     */
    val TEST_MODE: Boolean
        get() = BuildConfig.useTestAds
    
    // Google sample test ad unit IDs — always return test ads for every request
    private const val ANDROID_TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val IOS_TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/2934735716"

    /**
     * Get the platform-specific banner ad unit ID.
     * Test mode (debug / TestFlight / internal) → Google sample units.
     * Production store → real units.
     */
    fun getBannerAdUnitId(): String {
        val platform = getPlatform()
        val isAndroid = platform.name.startsWith("Android")
        return when {
            TEST_MODE && isAndroid -> ANDROID_TEST_BANNER_AD_UNIT_ID
            TEST_MODE && !isAndroid -> IOS_TEST_BANNER_AD_UNIT_ID
            isAndroid -> ANDROID_BANNER_AD_UNIT_ID
            else -> IOS_BANNER_AD_UNIT_ID
        }
    }

}