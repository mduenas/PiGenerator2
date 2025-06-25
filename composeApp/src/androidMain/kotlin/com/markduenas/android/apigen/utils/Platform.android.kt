package com.markduenas.android.apigen.utils

actual fun getCurrentTimeMillis(): Long = System.currentTimeMillis()

actual fun formatString(format: String, vararg args: Any): String = String.format(format, *args)