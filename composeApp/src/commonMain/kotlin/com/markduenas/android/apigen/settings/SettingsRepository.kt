package com.markduenas.android.apigen.settings

import kotlinx.coroutines.flow.Flow

/**
 * Platform-agnostic interface for storing and retrieving user preferences
 */
expect class SettingsRepository {
    /**
     * Flow of current user preferences
     */
    val preferencesFlow: Flow<UserPreferences>

    /**
     * Get whether ads have been removed
     */
    suspend fun getAdsRemoved(): Boolean

    /**
     * Set whether ads have been removed
     */
    suspend fun setAdsRemoved(removed: Boolean)

    /**
     * Store the purchase token for verification
     */
    suspend fun setPurchaseToken(token: String?)

    /**
     * Get the stored purchase token
     */
    suspend fun getPurchaseToken(): String?

    /**
     * Clear all preferences (for testing/reset)
     */
    suspend fun clearAll()
}

/**
 * Factory function to get platform-specific SettingsRepository
 */
expect fun getSettingsRepository(): SettingsRepository
