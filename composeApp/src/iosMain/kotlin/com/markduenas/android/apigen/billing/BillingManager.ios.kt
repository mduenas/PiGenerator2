package com.markduenas.android.apigen.billing

import com.markduenas.android.apigen.settings.getSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * iOS implementation of BillingManager
 *
 * This implementation uses a Swift bridge pattern similar to the AdMob integration.
 * The actual StoreKit calls are made from Swift code, which registers callbacks
 * with this Kotlin layer.
 */
actual class BillingManager {

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    actual val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private val _isAdRemovalPurchased = MutableStateFlow(false)
    actual val isAdRemovalPurchased: StateFlow<Boolean> = _isAdRemovalPurchased.asStateFlow()

    private val settingsRepository = getSettingsRepository()
    private val scope = CoroutineScope(Dispatchers.Main)

    actual suspend fun initialize() {
        // Check existing purchase status from local storage
        val adsRemoved = settingsRepository.getAdsRemoved()
        _isAdRemovalPurchased.value = adsRemoved

        // Initialize the Swift StoreKit manager if available
        storeKitManagerInitializer?.invoke()
    }

    actual suspend fun loadProductDetails() {
        _purchaseState.value = PurchaseState.Loading

        // Call Swift to load product details
        val loaded = storeKitProductLoader?.invoke(BillingConstants.PRODUCT_ID_REMOVE_ADS_IOS)

        if (loaded != true) {
            // Fallback to default display values if StoreKit is not available
            _purchaseState.value = PurchaseState.ProductLoaded(
                productId = BillingConstants.PRODUCT_ID_REMOVE_ADS_IOS,
                title = BillingConstants.REMOVE_ADS_TITLE,
                description = BillingConstants.REMOVE_ADS_DESCRIPTION,
                price = BillingConstants.REMOVE_ADS_PRICE_DISPLAY
            )
        }
    }

    actual suspend fun purchaseRemoveAds(): Boolean {
        _purchaseState.value = PurchaseState.Loading

        // Call Swift to initiate purchase
        val started = storeKitPurchaseInitiator?.invoke(BillingConstants.PRODUCT_ID_REMOVE_ADS_IOS)

        if (started != true) {
            _purchaseState.value = PurchaseState.Error("Unable to start purchase flow")
            return false
        }

        return true
    }

    actual suspend fun restorePurchases(): PurchaseResult {
        _purchaseState.value = PurchaseState.Loading

        // Call Swift to restore purchases
        val restored = storeKitRestoreHandler?.invoke()

        return if (restored == true) {
            val adsRemoved = settingsRepository.getAdsRemoved()
            if (adsRemoved) {
                _isAdRemovalPurchased.value = true
                _purchaseState.value = PurchaseState.Success(BillingConstants.PRODUCT_ID_REMOVE_ADS_IOS)
                PurchaseResult.Success("restored")
            } else {
                _purchaseState.value = PurchaseState.Idle
                PurchaseResult.Error("No previous purchase found")
            }
        } else {
            _purchaseState.value = PurchaseState.Error("Failed to restore purchases")
            PurchaseResult.Error("Failed to restore purchases")
        }
    }

    actual suspend fun checkPurchaseStatus(): Boolean {
        val adsRemoved = settingsRepository.getAdsRemoved()
        if (adsRemoved) {
            _isAdRemovalPurchased.value = true
            return true
        }

        // Try to restore from App Store
        val result = restorePurchases()
        return result is PurchaseResult.Success
    }

    actual fun cleanup() {
        // Nothing to clean up for iOS StoreKit
    }

    // Callbacks called from Swift side
    fun onProductLoaded(productId: String, title: String, description: String, price: String) {
        _purchaseState.value = PurchaseState.ProductLoaded(
            productId = productId,
            title = title,
            description = description,
            price = price
        )
    }

    fun onPurchaseSuccess(productId: String, transactionId: String) {
        scope.launch {
            settingsRepository.setAdsRemoved(true)
            settingsRepository.setPurchaseToken(transactionId)
            _isAdRemovalPurchased.value = true
            _purchaseState.value = PurchaseState.Success(productId)
        }
    }

    fun onPurchaseError(message: String) {
        _purchaseState.value = PurchaseState.Error(message)
    }

    fun onPurchaseCancelled() {
        _purchaseState.value = PurchaseState.Cancelled
    }

    fun onRestoreSuccess() {
        scope.launch {
            settingsRepository.setAdsRemoved(true)
            _isAdRemovalPurchased.value = true
            _purchaseState.value = PurchaseState.Success(BillingConstants.PRODUCT_ID_REMOVE_ADS_IOS)
        }
    }
}

// Swift bridge - set these from Swift side during app initialization
var storeKitManagerInitializer: (() -> Unit)? = null
var storeKitProductLoader: ((String) -> Boolean)? = null
var storeKitPurchaseInitiator: ((String) -> Boolean)? = null
var storeKitRestoreHandler: (() -> Boolean)? = null

private var billingManagerInstance: BillingManager? = null

actual fun getBillingManager(): BillingManager {
    if (billingManagerInstance == null) {
        billingManagerInstance = BillingManager()
    }
    return billingManagerInstance!!
}

/**
 * Get the singleton billing manager instance for use from Swift
 */
fun getBillingManagerForSwift(): BillingManager = getBillingManager()
