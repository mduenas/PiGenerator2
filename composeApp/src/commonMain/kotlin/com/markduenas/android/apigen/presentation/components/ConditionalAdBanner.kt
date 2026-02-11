package com.markduenas.android.apigen.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.markduenas.android.apigen.billing.getBillingManager
import com.markduenas.android.apigen.config.AdMobConstants

/**
 * A wrapper component that conditionally displays an ad banner
 * based on whether the user has purchased ad removal.
 */
@Composable
fun ConditionalAdBanner(
    modifier: Modifier = Modifier
) {
    val billingManager = getBillingManager()
    val isAdRemovalPurchased by billingManager.isAdRemovalPurchased.collectAsState()

    // Only show ads if:
    // 1. AdMob is enabled
    // 2. User has not purchased ad removal
    if (AdMobConstants.ADMOB_ENABLED && !isAdRemovalPurchased) {
        AdMobBanner(
            adUnitId = AdMobConstants.getBannerAdUnitId(),
            modifier = modifier
        )
    }
}
