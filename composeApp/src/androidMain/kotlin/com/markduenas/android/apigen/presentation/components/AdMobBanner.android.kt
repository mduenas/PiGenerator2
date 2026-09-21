package com.markduenas.android.apigen.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.markduenas.android.apigen.config.AdMobConstants
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember


/**
 * Android implementation of AdMob banner ad
 */
@Composable
actual fun AdMobBanner(
    adUnitId: String,
    modifier: Modifier
) {
    println("AdMob: Android AdMobBanner called with adUnitId: $adUnitId")
    val context = LocalContext.current
    
    // Initialize MobileAds once with test device configuration
    LaunchedEffect(Unit) {
        MobileAds.initialize(context) {
            println("AdMob: MobileAds initialized")
        }
        
        // Configure test devices for debug builds
        // Google logs your device's real hash to Logcat the first time an ad
        // loads on it: "Use RequestConfiguration.Builder().setTestDeviceIds(...)"
        // - copy that hash into the list below so dev/QA taps never count as
        // real ad traffic.
        val testDeviceIds = listOf(
            AdRequest.DEVICE_ID_EMULATOR
            // "ADD_YOUR_REAL_DEVICE_HASH_HERE",
        )
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()
        MobileAds.setRequestConfiguration(configuration)
        println("AdMob: Test device configuration set")
    }
    
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(AdMobConstants.BANNER_HEIGHT_DP.dp),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                setAdUnitId(adUnitId)
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}