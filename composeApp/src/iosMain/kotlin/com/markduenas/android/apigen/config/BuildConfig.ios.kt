package com.markduenas.android.apigen.config

import platform.Foundation.NSBundle

/**
 * iOS BuildConfig.
 * - isDebug: local Xcode / Simulator only
 * - useTestAds: debug OR TestFlight (sandbox receipt)
 * - App Store production: useTestAds = false
 */
actual object BuildConfig {
    actual val isDebug: Boolean
        get() = isLocalDebugBuild()

    actual val buildType: String
        get() = when {
            isDebug -> "debug"
            isTestFlightBuild() -> "testflight"
            else -> "release"
        }

    actual val useTestAds: Boolean
        get() = isDebug || isTestFlightBuild()
}

private fun isLocalDebugBuild(): Boolean {
    val bundlePath = NSBundle.mainBundle.bundlePath
    // Local Xcode runs only — not TestFlight / App Store install paths
    return bundlePath.contains("Debug", ignoreCase = true) ||
        bundlePath.contains("Simulator", ignoreCase = true) ||
        bundlePath.contains("CoreSimulator", ignoreCase = true) ||
        bundlePath.contains("DerivedData", ignoreCase = true)
}

/**
 * TestFlight installs use a sandbox App Store receipt
 * (path ends with / contains "sandboxReceipt").
 */
private fun isTestFlightBuild(): Boolean {
    val receiptUrl = NSBundle.mainBundle.appStoreReceiptURL ?: return false
    val path = receiptUrl.path ?: return false
    return path.contains("sandboxReceipt", ignoreCase = true)
}
