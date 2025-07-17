package com.markduenas.android.apigen.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.markduenas.android.apigen.config.AdMobConstants
import com.markduenas.android.apigen.config.BuildConfig
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController

/**
 * Global factory for creating AdMob view controllers from Swift
 */
var adMobViewControllerFactory: ((String) -> UIViewController)? = null

/**
 * iOS implementation of AdMobManager using UIViewController
 */
actual class AdMobManager {
    
    @OptIn(ExperimentalForeignApi::class)
    @Composable
    actual fun BannerAdView(
        adUnitId: String,
        modifier: Modifier
    ) {
        if (BuildConfig.isDebug) {
            // Show placeholder in debug mode
            AdMobBannerPlaceholder(modifier)
        } else {
            // Use native iOS AdMob implementation
            UIKitViewController(
                factory = {
                    AdMobBannerViewController(adUnitId)
                },
                modifier = modifier
                    .fillMaxWidth()
                    .height(AdMobConstants.BANNER_HEIGHT_DP.dp)
            )
        }
    }
}

/**
 * Creates platform-specific AdMobManager instance
 */
actual fun getAdMobManager(): AdMobManager = AdMobManager()

/**
 * Creates a UIViewController that manages the AdMob banner using Swift implementation
 */
@OptIn(ExperimentalForeignApi::class)
private fun AdMobBannerViewController(adUnitId: String): UIViewController {
    println("Creating AdMob banner view controller for: $adUnitId")
    
    // Use the Swift factory if available, otherwise fall back
    return adMobViewControllerFactory?.invoke(adUnitId) 
        ?: run {
            println("AdMob factory not set, using fallback implementation")
            createFallbackViewController(adUnitId)
        }
}

/**
 * Fallback implementation when Swift integration is not available
 */
@OptIn(ExperimentalForeignApi::class)
private fun createFallbackViewController(adUnitId: String): UIViewController {
    return platform.UIKit.UIViewController().apply {
        view.backgroundColor = platform.UIKit.UIColor(red = 0.95, green = 0.95, blue = 0.95, alpha = 1.0)
        
        val container = platform.UIKit.UIView()
        container.backgroundColor = platform.UIKit.UIColor.whiteColor
        container.layer.cornerRadius = 8.0
        container.layer.borderWidth = 1.0
        container.layer.borderColor = platform.UIKit.UIColor(red = 0.85, green = 0.85, blue = 0.85, alpha = 1.0).CGColor
        view.addSubview(container)
        
        val titleLabel = platform.UIKit.UILabel()
        titleLabel.text = if (BuildConfig.isDebug) "AdMob Debug Mode" else "AdMob Ready"
        titleLabel.textColor = platform.UIKit.UIColor(red = 0.2, green = 0.4, blue = 0.8, alpha = 1.0)
        titleLabel.font = platform.UIKit.UIFont.boldSystemFontOfSize(16.0)
        titleLabel.textAlignment = platform.UIKit.NSTextAlignmentCenter
        container.addSubview(titleLabel)
        
        val subtitleLabel = platform.UIKit.UILabel()
        subtitleLabel.text = "Ad Unit: ...${adUnitId.takeLast(8)}"
        subtitleLabel.textColor = platform.UIKit.UIColor.darkGrayColor
        subtitleLabel.font = platform.UIKit.UIFont.systemFontOfSize(12.0)
        subtitleLabel.textAlignment = platform.UIKit.NSTextAlignmentCenter
        container.addSubview(subtitleLabel)
        
        val statusLabel = platform.UIKit.UILabel()
        statusLabel.text = if (BuildConfig.isDebug) {
            "Real ads will show in production builds"
        } else {
            "Swift AdMobViewController integration available"
        }
        statusLabel.textColor = platform.UIKit.UIColor(red = 0.4, green = 0.6, blue = 0.4, alpha = 1.0)
        statusLabel.font = platform.UIKit.UIFont.systemFontOfSize(10.0)
        statusLabel.textAlignment = platform.UIKit.NSTextAlignmentCenter
        container.addSubview(statusLabel)
        
        // Setup constraints
        container.translatesAutoresizingMaskIntoConstraints = false
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        subtitleLabel.translatesAutoresizingMaskIntoConstraints = false
        statusLabel.translatesAutoresizingMaskIntoConstraints = false
        
        // Container constraints
        container.centerXAnchor.constraintEqualToAnchor(view.centerXAnchor).active = true
        container.centerYAnchor.constraintEqualToAnchor(view.centerYAnchor).active = true
        container.widthAnchor.constraintEqualToAnchor(view.widthAnchor, multiplier = 0.9).active = true
        container.heightAnchor.constraintEqualToConstant(AdMobConstants.BANNER_HEIGHT_DP.toDouble()).active = true
        
        // Label constraints
        titleLabel.centerXAnchor.constraintEqualToAnchor(container.centerXAnchor).active = true
        titleLabel.topAnchor.constraintEqualToAnchor(container.topAnchor, 8.0).active = true
        
        subtitleLabel.centerXAnchor.constraintEqualToAnchor(container.centerXAnchor).active = true
        subtitleLabel.topAnchor.constraintEqualToAnchor(titleLabel.bottomAnchor, 4.0).active = true
        
        statusLabel.centerXAnchor.constraintEqualToAnchor(container.centerXAnchor).active = true
        statusLabel.bottomAnchor.constraintEqualToAnchor(container.bottomAnchor, -8.0).active = true
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