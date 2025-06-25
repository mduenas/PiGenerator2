package com.markduenas.android.apigen

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform