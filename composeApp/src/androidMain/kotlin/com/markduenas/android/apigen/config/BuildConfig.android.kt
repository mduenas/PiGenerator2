package com.markduenas.android.apigen.config

import android.content.pm.ApplicationInfo
import com.markduenas.android.apigen.io.getAndroidContext

/**
 * Android implementation of BuildConfig using ApplicationInfo to detect debug builds
 */
actual object BuildConfig {
    actual val isDebug: Boolean
        get() {
            return try {
                val context = getAndroidContext()
                val appInfo = context.applicationInfo
                (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            } catch (e: Exception) {
                // Fallback: assume debug if we can't determine
                true
            }
        }
    
    actual val buildType: String
        get() = if (isDebug) "debug" else "release"
}