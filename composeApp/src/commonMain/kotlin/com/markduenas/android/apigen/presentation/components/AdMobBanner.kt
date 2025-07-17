package com.markduenas.android.apigen.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Cross-platform AdMob banner ad component
 */
@Composable
expect fun AdMobBanner(
    adUnitId: String,
    modifier: Modifier = Modifier
)