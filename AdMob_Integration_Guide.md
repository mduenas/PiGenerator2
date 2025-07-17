# AdMob Integration Guide for Kotlin Multiplatform Projects

This document provides a comprehensive, proven guide for AdMob integration in Kotlin Multiplatform (KMP) projects. This approach has been successfully implemented and tested in multiple production apps including HABa-Tracker and PiGenerator2.

## Overview

This AdMob integration follows a proven cross-platform architecture that:
- Uses native AdMob SDKs for optimal performance on both platforms
- Provides unified Kotlin interface across Android and iOS
- Automatically switches between test/production ads based on build type
- Includes graceful fallbacks and comprehensive error handling
- Uses modern Swift bridge pattern for iOS native integration
- Supports debug placeholders that don't consume real ad inventory

## Why This Approach Works

After testing multiple AdMob integration patterns, this approach was chosen because:
- **Proven in Production**: Successfully deployed in multiple App Store apps
- **Modern API**: Uses latest AdMob SDK features (`BannerView` instead of deprecated `GADBannerView`)
- **Simple Structure**: Single Swift file, clean factory pattern
- **Debug-Friendly**: Realistic placeholders prevent real ad requests during development
- **Future-Proof**: Uses expect/actual pattern that scales with KMP evolution

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Common Interface                         │
│  AdMobManager.kt (expect/actual)                           │
│  AdMobBanner.kt (wrapper)                                  │
│  AdMobConstants.kt (configuration)                         │
└─────────────────────────────────────────────────────────────┘
                              │
                ┌─────────────┴─────────────┐
                │                           │
      ┌─────────▼─────────┐        ┌────────▼────────┐
      │   Android Impl    │        │   iOS Impl      │
      │ AdMobManager.kt   │        │ AdMobManager.kt │
      │ Uses AndroidView  │        │ Uses UIKit      │
      │ Google Play Ads   │        │ Swift Bridge    │
      └───────────────────┘        └─────────────────┘
                                            │
                                   ┌────────▼────────┐
                                   │  Swift Bridge   │
                                   │ AdMobBanner-    │
                                   │ ViewController  │
                                   │ Native iOS SDK  │
                                   └─────────────────┘
```

## Step-by-Step Implementation Guide

### Step 1: Dependencies Setup

**Android Dependencies** in `composeApp/build.gradle.kts`:
```kotlin
androidMain.dependencies {
    implementation("com.google.android.gms:play-services-ads:23.6.0")
}
```

**iOS Dependencies** in `composeApp/build.gradle.kts`:
```kotlin
cocoapods {
    pod("Google-Mobile-Ads-SDK")
}
```

**iOS Podfile** at `iosApp/Podfile`:
```ruby
platform :ios, '12.0'
target 'iosApp' do
  use_frameworks!
  pod 'Google-Mobile-Ads-SDK'
  pod 'composeApp', :path => '../composeApp'
end
```

> **Important**: Always run `pod install` after changing the Podfile and use `.xcworkspace` not `.xcodeproj`

### Step 2: Configuration Setup

**Create AdMob Configuration** at `composeApp/src/commonMain/kotlin/[your.package]/config/AdMobConfig.kt`:

> **Critical**: Replace the production ad unit IDs with your actual AdMob ad unit IDs from Google AdMob console

```kotlin
object AdMobConfig {
    // Production Ad Unit IDs
    private const val PROD_ANDROID_BANNER_AD_UNIT_ID = "ca-app-pub-7540731406850248/6300978111"
    private const val PROD_IOS_BANNER_AD_UNIT_ID = "ca-app-pub-7540731406850248/7811996861"
    
    // Test Ad Unit IDs (Google's test units)
    private const val TEST_ANDROID_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_IOS_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/2934735716"
    
    // Automatic test/production switching
    val TEST_MODE: Boolean get() = BuildConfig.isDebug
    
    val ANDROID_BANNER_AD_UNIT_ID: String
        get() = if (TEST_MODE) TEST_ANDROID_BANNER_AD_UNIT_ID else PROD_ANDROID_BANNER_AD_UNIT_ID
        
    val IOS_BANNER_AD_UNIT_ID: String
        get() = if (TEST_MODE) TEST_IOS_BANNER_AD_UNIT_ID else PROD_IOS_BANNER_AD_UNIT_ID
}
```

**Create Build Detection** at `composeApp/src/commonMain/kotlin/[your.package]/config/BuildConfig.kt`:

> **Note**: This automatically detects debug vs release builds to switch between test and production ads

```kotlin
expect object BuildConfig {
    val isDebug: Boolean
    val buildType: String
}
```

**Android Implementation:**
```kotlin
actual object BuildConfig {
    actual val isDebug: Boolean
        get() {
            val context = getAndroidContext()
            val appInfo = context.applicationInfo
            return (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        }
}
```

**iOS Implementation:**
```kotlin
actual object BuildConfig {
    actual val isDebug: Boolean
        get() {
            val bundlePath = NSBundle.mainBundle.bundlePath
            return bundlePath.contains("Debug", ignoreCase = true) || 
                   bundlePath.contains("Simulator", ignoreCase = true)
        }
}
```

### Step 3: Kotlin Implementation

**Create Common Interface** at `composeApp/src/commonMain/kotlin/[your.package]/ads/AdMobManager.kt`:

```kotlin
expect class AdMobManager {
    @Composable
    fun BannerAdView(adUnitId: String, modifier: Modifier = Modifier)
    fun initialize()
    fun isReady(): Boolean
}

expect val GlobalAdMobManager: AdMobManager
```

**Create Android Implementation** at `composeApp/src/androidMain/kotlin/[your.package]/ads/AdMobManager.android.kt`:

```kotlin
actual class AdMobManager {
    @Composable
    actual fun BannerAdView(adUnitId: String, modifier: Modifier) {
        AndroidView(
            modifier = modifier.fillMaxWidth().height(50.dp),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    setAdUnitId(adUnitId)
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
```

**Create iOS Implementation** at `composeApp/src/iosMain/kotlin/[your.package]/ads/AdMobManager.ios.kt`:

```kotlin
// Factory function set from Swift
var adMobViewControllerFactory: ((String) -> UIViewController)? = null

actual class AdMobManager {
    @Composable
    actual fun BannerAdView(adUnitId: String, modifier: Modifier) {
        if (BuildConfig.isDebug) {
            AdMobBannerPlaceholder(modifier)
        } else {
            UIKitViewController(
                factory = { adMobViewControllerFactory!!(adUnitId) },
                modifier = modifier
            )
        }
    }
}
```

### Step 4: iOS Native Implementation

**Create Swift Controller** at `iosApp/iosApp/AdMobViewController.swift`:

> **Critical**: This must be named exactly `AdMobViewController` and use the modern `BannerView` API

```swift
import UIKit
import GoogleMobileAds
import ComposeApp

class AdMobViewController: UIViewController {
    private var bannerView: BannerView!
    private let adUnitId: String
    
    init(adUnitId: String) {
        self.adUnitId = adUnitId
        super.init(nibName: nil, bundle: nil)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupAdMobBanner()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        loadAd()
    }
    
    private func setupAdMobBanner() {
        bannerView = BannerView(adSize: AdSizeBanner)
        bannerView.adUnitID = adUnitId
        bannerView.rootViewController = self
        bannerView.delegate = self
        
        bannerView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(bannerView)
        
        NSLayoutConstraint.activate([
            bannerView.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            bannerView.centerYAnchor.constraint(equalTo: view.centerYAnchor),
            bannerView.widthAnchor.constraint(equalTo: view.widthAnchor),
            bannerView.heightAnchor.constraint(equalToConstant: AdSizeBanner.size.height)
        ])
    }
    
    private func loadAd() {
        let request = Request()
        bannerView.load(request)
    }
}

extension AdMobViewController: BannerViewDelegate {
    // ... delegate methods
}
```

**Update App Entry Point** at `iosApp/iosApp/iOSApp.swift`:

> **Important**: This registers the factory function that connects Kotlin to Swift

```swift
@main
struct iOSApp: App {
    init() {
        MobileAds.shared.start(completionHandler: nil)
        
        // Set up the AdMob factory for Kotlin interop
        AdMobManager_iosKt.adMobViewControllerFactory = { adUnitId in
            return AdMobViewController(adUnitId: adUnitId)
        }
    }
}
```

### Step 5: Create Wrapper Component (Optional)

**Create UI Component** at `composeApp/src/commonMain/kotlin/[your.package]/ui/components/AdMobBanner.kt`:

```kotlin
@Composable
fun AdMobBanner(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    GlobalAdMobManager.BannerAdView(
        adUnitId = adUnitId,
        modifier = modifier
    )
}
```

## Usage in UI

### Simple Banner Usage

```kotlin
@Composable
fun MyScreen() {
    Column {
        // Your content
        Text("My App Content")
        
        // AdMob banner
        if (AdMobConstants.ADMOB_ENABLED) {
            AdMobBanner(
                adUnitId = AdMobConstants.getBannerAdUnitId(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

### Using AdMobManager Directly

```kotlin
@Composable
fun MyScreen() {
    Column {
        // Your content
        Text("My App Content")
        
        // AdMob banner via manager
        GlobalAdMobManager.BannerAdView(
            adUnitId = AdMobConstants.getBannerAdUnitId(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
```

## Implementation Checklist

Use this checklist to ensure correct implementation:

### ✅ Dependencies
- [ ] Added `play-services-ads:23.6.0` to Android dependencies
- [ ] Added `Google-Mobile-Ads-SDK` pod to iOS CocoaPods
- [ ] Ran `pod install` after updating Podfile
- [ ] Using `.xcworkspace` file, not `.xcodeproj`

### ✅ Configuration
- [ ] Created `AdMobConfig.kt` with your production ad unit IDs
- [ ] Created `BuildConfig.kt` with expect/actual implementations
- [ ] Test ad unit IDs are Google's official test IDs
- [ ] Production ad unit IDs match your AdMob console

### ✅ Kotlin Implementation
- [ ] Created common `AdMobManager.kt` interface
- [ ] Implemented `AdMobManager.android.kt` using AndroidView
- [ ] Implemented `AdMobManager.ios.kt` with factory pattern
- [ ] Used `@OptIn(ExperimentalForeignApi::class)` for iOS

### ✅ iOS Native Implementation
- [ ] Created single `AdMobViewController.swift` file (not multiple files)
- [ ] Used modern `BannerView` API (not deprecated `GADBannerView`)
- [ ] Used `AdSizeBanner` (not `GADAdSizeBanner`)
- [ ] Used `Request` (not `GADRequest`)
- [ ] Used `BannerViewDelegate` (not `GADBannerViewDelegate`)
- [ ] Registered factory in `iOSApp.swift` init method

### ✅ Testing
- [ ] Debug builds show test ads/placeholders
- [ ] Release builds show production ads
- [ ] No real ad requests in debug mode
- [ ] Factory registration working (no nil factory errors)

## Testing and Debug Mode

### Debug Mode Behavior
- **Android**: Shows real test ads using Google's test ad units
- **iOS**: Shows realistic placeholder ads (no network requests)

### Release Mode Behavior
- **Android**: Shows real production ads using your ad unit IDs
- **iOS**: Shows real production ads via Swift bridge

### Debug Detection
The system automatically detects debug/release builds:
- **Android**: Uses `ApplicationInfo.FLAG_DEBUGGABLE`
- **iOS**: Checks bundle path for "Debug", "Simulator", or "DerivedData"

## Build Process

### Android Build
```bash
./gradlew assembleDebug    # Debug build with test ads
./gradlew assembleRelease  # Release build with production ads
```

### iOS Build
1. Open `iosApp/iosApp.xcworkspace` in Xcode
2. Select scheme (Debug/Release)
3. Build and run

**Note**: Always use the `.xcworkspace` file, not `.xcodeproj`, because of CocoaPods integration.

## Common Mistakes to Avoid

### ❌ Don't Use Multiple iOS Files
**Wrong**: Having multiple Swift files like `AdMobBannerView.swift`, `AdMobBridge.swift`, `AdMobBannerViewController.swift`
**Right**: Single `AdMobViewController.swift` file

### ❌ Don't Use Deprecated APIs
**Wrong**: `GADBannerView`, `GADAdSizeBanner`, `GADRequest`, `GADBannerViewDelegate`
**Right**: `BannerView`, `AdSizeBanner`, `Request`, `BannerViewDelegate`

### ❌ Don't Mix Factory Patterns
**Wrong**: Using `UIViewRepresentable` alongside UIViewController factory
**Right**: Single factory pattern with UIViewController

### ❌ Don't Skip Factory Registration
**Wrong**: Forgetting to register factory in `iOSApp.swift`
**Right**: Always register in app init method

### ❌ Don't Use Wrong File Names
**Wrong**: `AdMobBannerViewController` class name
**Right**: `AdMobViewController` class name (matches HABa-Tracker)

## Migration from Other Approaches

If you have an existing AdMob implementation, follow these steps:

### Step 1: Clean Up Old Implementation
```bash
# Remove old files
rm iosApp/iosApp/AdMobBannerView.swift
rm iosApp/iosApp/AdMobBridge.swift
rm iosApp/iosApp/AdMobBannerViewController.swift  # If using old naming
```

### Step 2: Update API Usage
Replace all occurrences of:
- `GADBannerView` → `BannerView`
- `GADAdSizeBanner` → `AdSizeBanner`
- `GADRequest` → `Request`
- `GADBannerViewDelegate` → `BannerViewDelegate`

### Step 3: Verify Factory Registration
Ensure your `iOSApp.swift` has exactly this pattern:
```swift
AdMobManager_iosKt.adMobViewControllerFactory = { adUnitId in
    return AdMobViewController(adUnitId: adUnitId)
}
```

## Troubleshooting

### Common Issues

1. **iOS ads not showing in release**
   - Verify AdMob factory is registered in `iOSApp.swift`
   - Check that `AdMobViewController.swift` is included in Xcode project
   - Ensure `Google-Mobile-Ads-SDK` pod is installed
   - Verify using modern API (`BannerView` not `GADBannerView`)

2. **Android ads not loading**
   - Check that `play-services-ads` dependency is included
   - Verify ad unit IDs are correct
   - Ensure app has internet permission

3. **Build errors on iOS**
   - Run `pod install` in `iosApp` directory
   - Clean build folder in Xcode
   - Ensure iOS deployment target is 12.0 or higher
   - Check for multiple conflicting Swift files

4. **Factory null pointer errors**
   - Verify factory registration happens before any ad calls
   - Check factory is registered in `iOSApp.swift` init method
   - Ensure import `ComposeApp` in Swift file

### Debug Commands

```bash
# Check CocoaPods installation
cd iosApp && pod install

# Clean and rebuild
./gradlew clean
./gradlew build

# Android debug build
./gradlew installDebug

# Check for AdMob in logs
adb logcat | grep -i admob  # Android
# For iOS, check Xcode console
```

## File Structure

```
composeApp/src/
├── commonMain/kotlin/com/markduenas/android/apigen/
│   ├── ads/
│   │   └── AdMobManager.kt                 # Common interface
│   ├── config/
│   │   ├── AdMobConfig.kt                  # Ad unit configuration
│   │   └── BuildConfig.kt                  # Build detection interface
│   ├── data/admob/
│   │   └── AdMobConstants.kt               # Constants and utilities
│   └── ui/components/
│       └── AdMobBanner.kt                  # Legacy wrapper
├── androidMain/kotlin/com/markduenas/android/apigen/
│   ├── ads/
│   │   └── AdMobManager.android.kt         # Android implementation
│   └── config/
│       └── BuildConfig.android.kt          # Android build detection
└── iosMain/kotlin/com/markduenas/android/apigen/
    ├── ads/
    │   └── AdMobManager.ios.kt             # iOS implementation
    └── config/
        └── BuildConfig.ios.kt              # iOS build detection

iosApp/iosApp/
├── AdMobViewController.swift              # Native iOS AdMob
├── iOSApp.swift                           # Factory registration
└── Podfile                                # CocoaPods configuration
```

## Key Features

1. **Cross-Platform**: Single interface works on both Android and iOS
2. **Native Performance**: Uses native AdMob SDKs on both platforms
3. **Automatic Test/Production**: Switches ad units based on build type
4. **Graceful Fallbacks**: Shows placeholders when ads fail to load
5. **Type Safety**: Full Kotlin type safety across platforms
6. **Maintainable**: Clean separation of concerns and centralized configuration

## Applying to New Projects

To implement this AdMob integration in a new Kotlin Multiplatform project:

### 1. Replace Package Names
Throughout all code examples, replace:
- `com.markduenas.android.apigen` with your actual package name
- `[your.package]` placeholders with your actual package structure

### 2. Update Bundle Identifiers
In the iOS configuration:
- Update bundle identifier in Xcode project settings
- Ensure AdMob ad unit IDs match your bundle identifier in AdMob console

### 3. Get Your AdMob Ad Unit IDs
1. Go to [Google AdMob Console](https://apps.admob.com/)
2. Create or select your app
3. Create banner ad units for Android and iOS
4. Replace the production ad unit IDs in `AdMobConfig.kt`

### 4. Test Implementation
1. **Debug builds**: Should show test ads (Android) or placeholders (iOS)
2. **Release builds**: Should show your production ads
3. **Verify**: No real ad inventory consumed during development

### 5. Deployment Checklist
- [ ] All production ad unit IDs are correct
- [ ] Bundle identifiers match AdMob console
- [ ] Test builds show test ads only
- [ ] Release builds show production ads
- [ ] Factory registration working on iOS
- [ ] Modern AdMob API used throughout

## Production Validation

Before deploying to production:

1. **Create Test Flight build** and verify ads load correctly
2. **Test on real devices** (not just simulators)
3. **Verify ad unit IDs** match your AdMob console exactly
4. **Check ad revenue** appears in AdMob console after deployment
5. **Monitor crash reports** for any AdMob-related issues

## References and Sources

This implementation is based on successful production deployments in:
- **HABa-Tracker**: Habit tracking app (App Store)
- **PiGenerator2**: Pi calculation app (App Store)

Key learnings incorporated:
- Modern AdMob SDK usage (BannerView API)
- Single-file iOS implementation pattern
- Reliable Swift-Kotlin bridge architecture
- Production-tested debug/release switching

This implementation provides a robust, maintainable, and proven solution for AdMob integration in Kotlin Multiplatform projects.