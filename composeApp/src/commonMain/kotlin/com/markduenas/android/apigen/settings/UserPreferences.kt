package com.markduenas.android.apigen.settings

/**
 * User preferences data class for app settings
 */
data class UserPreferences(
    val adsRemoved: Boolean = false,
    val purchaseToken: String? = null
)
