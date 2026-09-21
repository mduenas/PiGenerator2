package com.markduenas.android.apigen.config

/**
 * Cross-platform build configuration detection
 * for ad configuration and other build-gated behavior.
 */
expect object BuildConfig {
    /**
     * True for local debug / debuggable builds only.
     * False for release, TestFlight, and Play (any track).
     */
    val isDebug: Boolean

    /**
     * Build type label ("debug", "release", "testflight", etc.)
     */
    val buildType: String

    /**
     * True when Google sample test ad units must be used:
     * - local debug / debuggable
     * - iOS TestFlight
     * - Android `internal` build type (USE_TEST_ADS=true)
     * - sideloaded non-Play release installs
     *
     * False only for production store distribution (Play Production / App Store).
     */
    val useTestAds: Boolean
}
