package com.markduenas.android.apigen.config

import platform.Foundation.NSBundle

/**
 * iOS implementation of BuildConfig
 * Detects debug builds by checking for common debug indicators
 */
actual object BuildConfig {
    actual val isDebug: Boolean
        get() {
            // Check if this is a debug build by looking for debug-specific bundle properties
            val bundle = NSBundle.mainBundle
            // In debug builds, the bundle path typically contains "Debug" or "Simulator"
            val bundlePath = bundle.bundlePath
            return bundlePath.contains("Debug", ignoreCase = true) || 
                   bundlePath.contains("Simulator", ignoreCase = true) ||
                   bundlePath.contains("DerivedData", ignoreCase = true)
        }
    
    actual val buildType: String
        get() = if (isDebug) "debug" else "release"
}