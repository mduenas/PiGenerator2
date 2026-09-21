package com.markduenas.android.apigen.config

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

/**
 * Android BuildConfig: debug via FLAG_DEBUGGABLE; test ads also for
 * internal builds (USE_TEST_ADS) and non-Play installs.
 */
actual object BuildConfig {
    actual val isDebug: Boolean
        get() {
            return try {
                val context = getAndroidContext()
                (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            } catch (_: Exception) {
                // Fail safe: prefer test ads if we cannot tell
                true
            }
        }

    actual val buildType: String
        get() = when {
            isDebug -> "debug"
            useTestAds -> "internal"
            else -> "release"
        }

    actual val useTestAds: Boolean
        get() {
            if (isDebug) return true
            return try {
                val context = getAndroidContext()
                if (readUseTestAdsField(context)) return true
                // Sideloaded / IDE release APKs — never treat as production ad traffic
                !isInstalledFromPlayStore(context)
            } catch (_: Exception) {
                true
            }
        }
}

private fun readUseTestAdsField(context: Context): Boolean {
    return try {
        Class.forName("${context.packageName}.BuildConfig")
            .getField("USE_TEST_ADS")
            .getBoolean(null)
    } catch (_: Exception) {
        false
    }
}

private fun isInstalledFromPlayStore(context: Context): Boolean {
    val playInstallers = setOf(
        "com.android.vending",
        "com.google.android.feedback"
    )
    val installer = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getInstallerPackageName(context.packageName)
        }
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }
    return installer != null && installer in playInstallers
}

private var androidContext: Context? = null

fun setAndroidContext(context: Context) {
    androidContext = context.applicationContext
}

fun getAndroidContext(): Context {
    return androidContext
        ?: throw IllegalStateException("Android context not set. Call setAndroidContext() from MainActivity.")
}
