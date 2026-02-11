package com.markduenas.android.apigen.billing

/**
 * Represents the state of a purchase operation
 */
sealed class PurchaseState {
    /**
     * No purchase operation in progress
     */
    object Idle : PurchaseState()

    /**
     * Purchase is being processed
     */
    object Loading : PurchaseState()

    /**
     * Purchase completed successfully
     */
    data class Success(val productId: String) : PurchaseState()

    /**
     * Purchase failed with an error
     */
    data class Error(val message: String, val code: Int? = null) : PurchaseState()

    /**
     * Purchase was cancelled by user
     */
    object Cancelled : PurchaseState()

    /**
     * Product info loaded and ready for purchase
     */
    data class ProductLoaded(
        val productId: String,
        val title: String,
        val description: String,
        val price: String
    ) : PurchaseState()
}

/**
 * Result of a purchase operation
 */
sealed class PurchaseResult {
    data class Success(val purchaseToken: String) : PurchaseResult()
    data class Error(val message: String, val code: Int? = null) : PurchaseResult()
    object Cancelled : PurchaseResult()
    object Pending : PurchaseResult()
}
