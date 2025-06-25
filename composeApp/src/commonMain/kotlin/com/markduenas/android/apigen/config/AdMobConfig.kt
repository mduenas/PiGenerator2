package com.markduenas.android.apigen.config

object AdMobConfig {
    // Production Ad Unit IDs - Replace these with your actual AdMob ad unit IDs
    private const val PROD_ANDROID_BANNER_AD_UNIT_ID = "ca-app-pub-7540731406850248/6300978111" // Replace with your actual ID
    private const val PROD_ANDROID_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-7540731406850248/8187094669" // Replace with your actual ID  
    private const val PROD_ANDROID_REWARDED_AD_UNIT_ID = "ca-app-pub-7540731406850248/5224354917" // Replace with your actual ID
    
    private const val PROD_IOS_BANNER_AD_UNIT_ID = "ca-app-pub-7540731406850248/7811996861" // Replace with your actual ID
    private const val PROD_IOS_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-7540731406850248/1441174175" // Replace with your actual ID
    private const val PROD_IOS_REWARDED_AD_UNIT_ID = "ca-app-pub-7540731406850248/1712485313" // Replace with your actual ID
    
    // Google's test ad unit IDs
    private const val TEST_ANDROID_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_ANDROID_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_ANDROID_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    
    private const val TEST_IOS_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/2934735716"
    private const val TEST_IOS_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/4411468910"
    private const val TEST_IOS_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/1712485313"
    
    /**
     * Automatically detects test mode based on build type
     * Returns true for debug builds, false for release builds
     */
    val TEST_MODE: Boolean
        get() = BuildConfig.isDebug
    
    /**
     * Returns the appropriate ad unit ID based on current build type
     */
    val ANDROID_BANNER_AD_UNIT_ID: String
        get() = if (TEST_MODE) TEST_ANDROID_BANNER_AD_UNIT_ID else PROD_ANDROID_BANNER_AD_UNIT_ID
        
    val ANDROID_INTERSTITIAL_AD_UNIT_ID: String
        get() = if (TEST_MODE) TEST_ANDROID_INTERSTITIAL_AD_UNIT_ID else PROD_ANDROID_INTERSTITIAL_AD_UNIT_ID
        
    val ANDROID_REWARDED_AD_UNIT_ID: String
        get() = if (TEST_MODE) TEST_ANDROID_REWARDED_AD_UNIT_ID else PROD_ANDROID_REWARDED_AD_UNIT_ID
    
    val IOS_BANNER_AD_UNIT_ID: String
        get() = if (TEST_MODE) TEST_IOS_BANNER_AD_UNIT_ID else PROD_IOS_BANNER_AD_UNIT_ID
        
    val IOS_INTERSTITIAL_AD_UNIT_ID: String
        get() = if (TEST_MODE) TEST_IOS_INTERSTITIAL_AD_UNIT_ID else PROD_IOS_INTERSTITIAL_AD_UNIT_ID
        
    val IOS_REWARDED_AD_UNIT_ID: String
        get() = if (TEST_MODE) TEST_IOS_REWARDED_AD_UNIT_ID else PROD_IOS_REWARDED_AD_UNIT_ID
}