package com.markduenas.android.apigen.config

/**
 * Cross-platform build configuration detection
 * Automatically detects debug/release builds for proper ad configuration
 */
expect object BuildConfig {
    /**
     * Returns true if this is a debug build, false for release builds
     */
    val isDebug: Boolean
    
    /**
     * Returns the build type as a string ("debug", "release", etc.)
     */
    val buildType: String
}