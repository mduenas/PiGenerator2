package com.markduenas.android.apigen.config

import android.content.Context
import android.content.pm.ApplicationInfo

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

// We'll need to set this from MainActivity or App initialization
private var androidContext: Context? = null

fun setAndroidContext(context: Context) {
    androidContext = context
}

fun getAndroidContext(): Context {
    return androidContext ?: throw IllegalStateException("Android context not set. Call setAndroidContext() from MainActivity.")
}