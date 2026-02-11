package com.markduenas.android.apigen.billing

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.*
import com.markduenas.android.apigen.config.getAndroidContext
import com.markduenas.android.apigen.settings.getSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android implementation of BillingManager using Google Play Billing Library
 */
actual class BillingManager : PurchasesUpdatedListener {

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    actual val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private val _isAdRemovalPurchased = MutableStateFlow(false)
    actual val isAdRemovalPurchased: StateFlow<Boolean> = _isAdRemovalPurchased.asStateFlow()

    private var billingClient: BillingClient? = null
    private var productDetails: ProductDetails? = null
    private var currentActivity: Activity? = null

    private val settingsRepository = getSettingsRepository()
    private val scope = CoroutineScope(Dispatchers.Main)

    fun setActivity(activity: Activity) {
        currentActivity = activity
    }

    actual suspend fun initialize() {
        val context = getAndroidContext()

        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        startConnection()

        // Check existing purchase status
        val adsRemoved = settingsRepository.getAdsRemoved()
        _isAdRemovalPurchased.value = adsRemoved
    }

    private suspend fun startConnection() = suspendCancellableCoroutine { continuation ->
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(true)
                } else {
                    _purchaseState.value = PurchaseState.Error(
                        "Billing setup failed: ${billingResult.debugMessage}",
                        billingResult.responseCode
                    )
                    continuation.resume(false)
                }
            }

            override fun onBillingServiceDisconnected() {
                _purchaseState.value = PurchaseState.Error("Billing service disconnected")
            }
        })
    }

    actual suspend fun loadProductDetails() {
        _purchaseState.value = PurchaseState.Loading

        // Check if billing client is ready
        val client = billingClient
        if (client == null) {
            Log.e("BillingManager", "Billing client is null!")
            _purchaseState.value = PurchaseState.Error("Billing client not initialized")
            return
        }

        if (!client.isReady) {
            Log.e("BillingManager", "Billing client is not ready! Reconnecting...")
            startConnection()
        }

        Log.d("BillingManager", "Billing client ready: ${client.isReady}")
        Log.d("BillingManager", "Querying for product: ${BillingConstants.PRODUCT_ID_REMOVE_ADS_ANDROID}")

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(BillingConstants.PRODUCT_ID_REMOVE_ADS_ANDROID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        client.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            Log.d("BillingManager", "Product query response: ${billingResult.responseCode}, products: ${productDetailsList.size}")
            Log.d("BillingManager", "Looking for product ID: ${BillingConstants.PRODUCT_ID_REMOVE_ADS_ANDROID}")

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                productDetails = productDetailsList.first()
                val details = productDetails!!
                val price = details.oneTimePurchaseOfferDetails?.formattedPrice
                    ?: BillingConstants.REMOVE_ADS_PRICE_DISPLAY

                Log.d("BillingManager", "Product loaded: ${details.productId}, price: $price")
                _purchaseState.value = PurchaseState.ProductLoaded(
                    productId = details.productId,
                    title = details.title,
                    description = details.description,
                    price = price
                )
            } else {
                val errorMsg = when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> "Product not found. Create it in Google Play Console."
                    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> "Billing service disconnected"
                    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> "Feature not supported"
                    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "Billing service unavailable"
                    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "Billing unavailable on this device"
                    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "Product not available"
                    BillingClient.BillingResponseCode.DEVELOPER_ERROR -> "Developer error - check product ID"
                    BillingClient.BillingResponseCode.ERROR -> "General error"
                    else -> "Unknown error (${billingResult.responseCode}): ${billingResult.debugMessage}"
                }
                Log.e("BillingManager", "Failed to load product: $errorMsg")
                _purchaseState.value = PurchaseState.Error(errorMsg, billingResult.responseCode)
            }
        }
    }

    actual suspend fun purchaseRemoveAds(): Boolean {
        val activity = currentActivity ?: return false
        val details = productDetails ?: return false

        _purchaseState.value = PurchaseState.Loading

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val result = billingClient?.launchBillingFlow(activity, billingFlowParams)
        return result?.responseCode == BillingClient.BillingResponseCode.OK
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Cancelled
            }
            else -> {
                _purchaseState.value = PurchaseState.Error(
                    billingResult.debugMessage ?: "Purchase failed",
                    billingResult.responseCode
                )
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Acknowledge the purchase if not already acknowledged
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        completePurchase(purchase)
                    } else {
                        _purchaseState.value = PurchaseState.Error(
                            "Failed to acknowledge purchase",
                            billingResult.responseCode
                        )
                    }
                }
            } else {
                completePurchase(purchase)
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            _purchaseState.value = PurchaseState.Loading
        }
    }

    private fun completePurchase(purchase: Purchase) {
        scope.launch {
            settingsRepository.setAdsRemoved(true)
            settingsRepository.setPurchaseToken(purchase.purchaseToken)
            _isAdRemovalPurchased.value = true
            _purchaseState.value = PurchaseState.Success(purchase.products.firstOrNull() ?: "")
        }
    }

    actual suspend fun restorePurchases(): PurchaseResult {
        _purchaseState.value = PurchaseState.Loading

        return suspendCancellableCoroutine { continuation ->
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            billingClient?.queryPurchasesAsync(params) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val removeAdsPurchase = purchases.find { purchase ->
                        purchase.products.contains(BillingConstants.PRODUCT_ID_REMOVE_ADS_ANDROID) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                    }

                    if (removeAdsPurchase != null) {
                        scope.launch {
                            settingsRepository.setAdsRemoved(true)
                            settingsRepository.setPurchaseToken(removeAdsPurchase.purchaseToken)
                            _isAdRemovalPurchased.value = true
                            _purchaseState.value = PurchaseState.Success(BillingConstants.PRODUCT_ID_REMOVE_ADS_ANDROID)
                        }
                        continuation.resume(PurchaseResult.Success(removeAdsPurchase.purchaseToken))
                    } else {
                        _purchaseState.value = PurchaseState.Idle
                        continuation.resume(PurchaseResult.Error("No previous purchase found"))
                    }
                } else {
                    _purchaseState.value = PurchaseState.Error(
                        billingResult.debugMessage ?: "Failed to restore purchases",
                        billingResult.responseCode
                    )
                    continuation.resume(PurchaseResult.Error(billingResult.debugMessage ?: "Failed"))
                }
            }
        }
    }

    actual suspend fun checkPurchaseStatus(): Boolean {
        val adsRemoved = settingsRepository.getAdsRemoved()
        if (adsRemoved) {
            _isAdRemovalPurchased.value = true
            return true
        }

        // Also check with billing client
        val result = restorePurchases()
        return result is PurchaseResult.Success
    }

    actual fun cleanup() {
        billingClient?.endConnection()
        billingClient = null
        productDetails = null
        currentActivity = null
    }
}

private var billingManagerInstance: BillingManager? = null

actual fun getBillingManager(): BillingManager {
    if (billingManagerInstance == null) {
        billingManagerInstance = BillingManager()
    }
    return billingManagerInstance!!
}
