import Foundation
import StoreKit
import ComposeApp

/// StoreKit manager for handling in-app purchases
/// Uses StoreKit 1 for iOS 12+ compatibility
class StoreKitManager: NSObject, SKProductsRequestDelegate, SKPaymentTransactionObserver {

    static let shared = StoreKitManager()

    private var products: [String: SKProduct] = [:]
    private var productsRequest: SKProductsRequest?
    private var billingManager: BillingManager?

    private override init() {
        super.init()
        SKPaymentQueue.default().add(self)
    }

    deinit {
        SKPaymentQueue.default().remove(self)
    }

    /// Initialize the StoreKit manager and set up Kotlin bridge
    func initialize() {
        billingManager = BillingManager_iosKt.getBillingManagerForSwift()

        // Set up the Kotlin bridge callbacks
        BillingManager_iosKt.storeKitManagerInitializer = {
            // Already initialized
        }

        BillingManager_iosKt.storeKitProductLoader = { [weak self] productId in
            self?.loadProduct(productId: productId)
            return KotlinBoolean(bool: true)
        }

        BillingManager_iosKt.storeKitPurchaseInitiator = { [weak self] productId in
            let result = self?.purchaseProduct(productId: productId) ?? false
            return KotlinBoolean(bool: result)
        }

        BillingManager_iosKt.storeKitRestoreHandler = { [weak self] in
            self?.restorePurchases()
            return KotlinBoolean(bool: true)
        }
    }

    // MARK: - Product Loading

    func loadProduct(productId: String) {
        let productIdentifiers = Set([productId])
        productsRequest = SKProductsRequest(productIdentifiers: productIdentifiers)
        productsRequest?.delegate = self
        productsRequest?.start()
    }

    func productsRequest(_ request: SKProductsRequest, didReceive response: SKProductsResponse) {
        DispatchQueue.main.async { [weak self] in
            for product in response.products {
                self?.products[product.productIdentifier] = product

                let price = self?.formatPrice(product: product) ?? "$1.99"

                self?.billingManager?.onProductLoaded(
                    productId: product.productIdentifier,
                    title: product.localizedTitle,
                    description: product.localizedDescription,
                    price: price
                )
            }

            // Log invalid product identifiers for debugging
            for invalidId in response.invalidProductIdentifiers {
                print("StoreKit: Invalid product identifier: \(invalidId)")
            }
        }
    }

    func request(_ request: SKRequest, didFailWithError error: Error) {
        DispatchQueue.main.async { [weak self] in
            self?.billingManager?.onPurchaseError(message: "Failed to load products: \(error.localizedDescription)")
        }
    }

    // MARK: - Purchase

    func purchaseProduct(productId: String) -> Bool {
        guard SKPaymentQueue.canMakePayments() else {
            billingManager?.onPurchaseError(message: "In-app purchases are disabled on this device")
            return false
        }

        guard let product = products[productId] else {
            // Product not loaded yet, try to load it first
            loadProduct(productId: productId)
            billingManager?.onPurchaseError(message: "Product not loaded. Please try again.")
            return false
        }

        let payment = SKPayment(product: product)
        SKPaymentQueue.default().add(payment)
        return true
    }

    // MARK: - Restore Purchases

    func restorePurchases() {
        SKPaymentQueue.default().restoreCompletedTransactions()
    }

    // MARK: - SKPaymentTransactionObserver

    func paymentQueue(_ queue: SKPaymentQueue, updatedTransactions transactions: [SKPaymentTransaction]) {
        for transaction in transactions {
            switch transaction.transactionState {
            case .purchased:
                handlePurchased(transaction)
            case .failed:
                handleFailed(transaction)
            case .restored:
                handleRestored(transaction)
            case .deferred:
                // Transaction is in the queue, but awaiting approval
                break
            case .purchasing:
                // Transaction is being processed
                break
            @unknown default:
                break
            }
        }
    }

    func paymentQueue(_ queue: SKPaymentQueue, restoreCompletedTransactionsFailedWithError error: Error) {
        DispatchQueue.main.async { [weak self] in
            self?.billingManager?.onPurchaseError(message: "Restore failed: \(error.localizedDescription)")
        }
    }

    func paymentQueueRestoreCompletedTransactionsFinished(_ queue: SKPaymentQueue) {
        DispatchQueue.main.async { [weak self] in
            // Check if any purchases were restored
            let removeAdsProductId = BillingConstants.shared.PRODUCT_ID_REMOVE_ADS_IOS
            let restoredRemoveAds = queue.transactions.contains { transaction in
                transaction.transactionState == .restored &&
                transaction.payment.productIdentifier == removeAdsProductId
            }

            if restoredRemoveAds {
                self?.billingManager?.onRestoreSuccess()
            }
        }
    }

    // MARK: - Transaction Handling

    private func handlePurchased(_ transaction: SKPaymentTransaction) {
        let productId = transaction.payment.productIdentifier
        let transactionId = transaction.transactionIdentifier ?? UUID().uuidString

        DispatchQueue.main.async { [weak self] in
            self?.billingManager?.onPurchaseSuccess(productId: productId, transactionId: transactionId)
        }

        SKPaymentQueue.default().finishTransaction(transaction)
    }

    private func handleFailed(_ transaction: SKPaymentTransaction) {
        DispatchQueue.main.async { [weak self] in
            if let error = transaction.error as? SKError {
                switch error.code {
                case .paymentCancelled:
                    self?.billingManager?.onPurchaseCancelled()
                default:
                    self?.billingManager?.onPurchaseError(message: error.localizedDescription)
                }
            } else if let error = transaction.error {
                self?.billingManager?.onPurchaseError(message: error.localizedDescription)
            }
        }

        SKPaymentQueue.default().finishTransaction(transaction)
    }

    private func handleRestored(_ transaction: SKPaymentTransaction) {
        let productId = transaction.payment.productIdentifier
        let transactionId = transaction.original?.transactionIdentifier ?? transaction.transactionIdentifier ?? UUID().uuidString

        DispatchQueue.main.async { [weak self] in
            self?.billingManager?.onPurchaseSuccess(productId: productId, transactionId: transactionId)
        }

        SKPaymentQueue.default().finishTransaction(transaction)
    }

    // MARK: - Helpers

    private func formatPrice(product: SKProduct) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.locale = product.priceLocale
        return formatter.string(from: product.price) ?? "$\(product.price)"
    }
}
