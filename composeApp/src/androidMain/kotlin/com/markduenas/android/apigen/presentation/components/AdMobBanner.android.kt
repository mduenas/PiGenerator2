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
import com.markduenas.android.apigen.config.AdMobConstants


/**
 * Android implementation of AdMob banner ad
 */
@Composable
actual fun AdMobBanner(
    adUnitId: String,
    modifier: Modifier
) {
    val context = LocalContext.current
    
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