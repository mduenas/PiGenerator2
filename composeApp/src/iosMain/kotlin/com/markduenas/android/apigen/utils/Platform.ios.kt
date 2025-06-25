package com.markduenas.android.apigen.utils

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun getCurrentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun formatString(format: String, vararg args: Any): String {
    // Simple implementation for iOS
    return when {
        format.contains("%02d%02d%04d") && args.size >= 3 -> {
            val month = args[0].toString().padStart(2, '0')
            val day = args[1].toString().padStart(2, '0')
            val year = args[2].toString().padStart(4, '0')
            "$month$day$year"
        }
        format.contains("%02d%02d%02d") && args.size >= 3 -> {
            val month = args[0].toString().padStart(2, '0')
            val day = args[1].toString().padStart(2, '0')
            val year = args[2].toString().padStart(2, '0')
            "$month$day$year"
        }
        format.contains("%d%d%d") && args.size >= 3 -> {
            "${args[0]}${args[1]}${args[2]}"
        }
        else -> args.joinToString("")
    }
}