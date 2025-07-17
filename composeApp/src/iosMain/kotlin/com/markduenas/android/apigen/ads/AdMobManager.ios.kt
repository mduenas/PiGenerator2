package com.markduenas.android.apigen.ads

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitViewController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.markduenas.android.apigen.config.BuildConfig
import com.markduenas.android.apigen.data.admob.AdMobConstants
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController

/**
 * Global factory function for creating AdMob view controllers
 * This is set from the Swift side during app initialization
 */
@OptIn(ExperimentalForeignApi::class)
var adMobViewControllerFactory: ((String) -> UIViewController)? = null

/**
 * iOS implementation of AdMobManager
 * Uses Swift bridge to integrate with Google Mobile Ads SDK
 */
actual class AdMobManager {
    
    private var initialized = false
    
    @OptIn(ExperimentalForeignApi::class)
    @Composable
    actual fun BannerAdView(
        adUnitId: String,
        modifier: Modifier
    ) {
        if (BuildConfig.isDebug) {
            // Debug mode - show realistic placeholder
            AdMobBannerPlaceholder(modifier)
        } else {
            // Production mode - use Swift bridge
            if (adMobViewControllerFactory != null) {
                UIKitViewController(
                    factory = { 
                        adMobViewControllerFactory!!(adUnitId)
                    },
                    modifier = modifier
                        .fillMaxWidth()
                        .height(AdMobConstants.BANNER_HEIGHT_DP.dp)
                )
            } else {
                // Fallback to placeholder if factory not set
                AdMobBannerPlaceholder(modifier)
            }
        }
    }
    
    actual fun initialize() {
        // iOS initialization happens in Swift/Objective-C code
        // This is just marking that we've attempted initialization
        initialized = true
    }
    
    actual fun isReady(): Boolean {
        return initialized && (BuildConfig.isDebug || adMobViewControllerFactory != null)
    }
}

/**
 * Debug mode placeholder that mimics typical mobile ads
 */
@Composable
private fun AdMobBannerPlaceholder(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AdMobConstants.BANNER_HEIGHT_DP.dp)
            .background(
                Color(0xFFF5F5F5),
                RoundedCornerShape(8.dp)
            )
            .border(
                BorderStroke(1.dp, Color(0xFFE0E0E0)),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Fake app icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(0xFF4285F4),
                        RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "π",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
            
            // Fake ad content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Pi Calculator Pro",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A73E8)
                )
                Text(
                    text = "Calculate digits • Free download",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF5F6368)
                )
            }
            
            // Fake install button
            Box(
                modifier = Modifier
                    .background(
                        Color(0xFF34A853),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Install",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
        
        // "Ad" indicator in corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(
                    Color(0xFFDADADA),
                    RoundedCornerShape(2.dp)
                )
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = "Ad",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF5F6368)
            )
        }
    }
}

/**
 * Global iOS AdMob manager instance
 */
actual val GlobalAdMobManager: AdMobManager = AdMobManager()