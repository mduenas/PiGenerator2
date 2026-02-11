package com.markduenas.android.apigen.billing

import com.markduenas.android.apigen.getPlatform

/**
 * Constants for in-app billing/purchases
 */
object BillingConstants {

    /**
     * Product ID for removing ads - Android (Google Play)
     */
    const val PRODUCT_ID_REMOVE_ADS_ANDROID = "com.markduenas.android.apigen.removeads"

    /**
     * Product ID for removing ads - iOS (App Store)
     */
    const val PRODUCT_ID_REMOVE_ADS_IOS = "com.markduenas.apigen.removeads"

    /**
     * Display price for UI (actual price is fetched from store)
     */
    const val REMOVE_ADS_PRICE_DISPLAY = "$1.99"

    /**
     * Product title for display
     */
    const val REMOVE_ADS_TITLE = "Remove Ads"

    /**
     * Product description for display
     */
    const val REMOVE_ADS_DESCRIPTION = "Remove all advertisements from the app forever with a one-time purchase."

    /**
     * Get the platform-specific product ID for removing ads
     */
    fun getRemoveAdsProductId(): String {
        val platform = getPlatform()
        return if (platform.name.startsWith("Android")) {
            PRODUCT_ID_REMOVE_ADS_ANDROID
        } else {
            PRODUCT_ID_REMOVE_ADS_IOS
        }
    }
}
