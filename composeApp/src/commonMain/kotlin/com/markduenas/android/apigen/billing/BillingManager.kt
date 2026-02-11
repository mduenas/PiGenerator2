package com.markduenas.android.apigen.billing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Platform-agnostic interface for managing in-app purchases
 */
expect class BillingManager {
    /**
     * Current state of the billing manager
     */
    val purchaseState: StateFlow<PurchaseState>

    /**
     * Whether the user has purchased ad removal
     */
    val isAdRemovalPurchased: StateFlow<Boolean>

    /**
     * Initialize the billing client and connect to the store
     */
    suspend fun initialize()

    /**
     * Load product details for the remove ads product
     */
    suspend fun loadProductDetails()

    /**
     * Start the purchase flow for removing ads
     * Returns true if the purchase flow was started successfully
     */
    suspend fun purchaseRemoveAds(): Boolean

    /**
     * Restore previous purchases (required for iOS, good practice for Android)
     */
    suspend fun restorePurchases(): PurchaseResult

    /**
     * Check if there's an active purchase for ad removal
     */
    suspend fun checkPurchaseStatus(): Boolean

    /**
     * Clean up billing client resources
     */
    fun cleanup()
}

/**
 * Factory function to get platform-specific BillingManager
 */
expect fun getBillingManager(): BillingManager
