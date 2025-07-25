# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Pi Generator is a Kotlin Multiplatform (KMP) mobile application targeting Android and iOS. Built with Compose Multiplatform, it provides pi calculation, memorization training, pattern exploration, and pi digit viewing features with integrated AdMob advertising.

## Build System and Dependencies

This project uses Gradle with Kotlin DSL and the following key plugins:
- Kotlin Multiplatform (`kotlinMultiplatform`)
- Android Application (`androidApplication`) 
- Compose Multiplatform (`composeMultiplatform`)
- Compose Compiler (`composeCompiler`)
- Kotlin CocoaPods (`kotlin("native.cocoapods")`)

### Build Commands

```bash
# Build the project
./gradlew build

# Build Android APK
./gradlew assembleDebug

# Build Android release
./gradlew assembleRelease

# Run Android app
./gradlew installDebug

# Clean build
./gradlew clean

# iOS builds require Xcode and should be done through iOS project
```

### Dependencies
- **AdMob Integration**: Google Mobile Ads SDK for both platforms
- **Compose**: Material3, Icons Extended, UI components
- **Lifecycle**: ViewModel and Runtime Compose
- **Testing**: Kotlin test, JUnit, Espresso

## Project Structure

```
composeApp/src/
   commonMain/kotlin/com/markduenas/android/apigen/
      calculation/          # Pi calculation algorithms (Machin, AGM-FFT, Spigot)
      memorization/         # Memory training game logic
      patterns/             # Pattern search functionality
      data/                 # Data models and results
      ui/
         screens/          # Main app screens (Calculator, Memorization, etc.)
         navigation/       # Navigation setup and routing
         components/       # Reusable UI components
      ads/                  # AdMob integration
   androidMain/kotlin/       # Android-specific implementations
   iosMain/kotlin/           # iOS-specific implementations

iosApp/                       # iOS app entry point and Swift code
```

## Architecture

The app follows a modular architecture with:

1. **Navigation**: Bottom navigation with 4 main screens (Calculator, Training, Explorer, Viewer)
2. **Pi Calculation**: Multiple algorithms for different performance characteristics
3. **Cross-platform Ad Integration**: Platform-specific AdMob implementations
4. **File I/O**: Platform-specific file reading for pre-calculated pi digits

### Main Screens
- **CalculatorScreen**: Multi-algorithm pi calculation with progress tracking
- **MemorizationScreen**: Gamified pi digit memorization training
- **PatternExplorerScreen**: Search for patterns within pi digits
- **PiViewerScreen**: Display and browse pre-calculated pi digits

## Platform-Specific Notes

### Android
- Target SDK: 35, Min SDK: 24
- Application ID: `com.markduenas.android.apigen`
- AdMob integration via Google Play Services

### iOS  
- Deployment target: iOS 12.0
- Uses CocoaPods for dependency management
- AdMob integration via Google Mobile Ads SDK pod
- Requires Xcode for iOS builds

## Ad Integration

The app includes platform-specific AdMob banner implementations:
- `AdMobBanner.kt` (common interface)
- `AdMobBanner.android.kt` (Android implementation)
- `AdMobBanner.ios.kt` (iOS implementation)

## Development Workflow

1. Make changes in `composeApp/src/commonMain` for shared code
2. Platform-specific code goes in respective `androidMain` or `iosMain` folders
3. Test Android builds with `./gradlew assembleDebug`
4. Test iOS builds through Xcode workspace at `iosApp/iosApp.xcworkspace`
5. AdMob integration requires platform-specific setup and testing

## Testing

- Common tests: `composeApp/src/commonTest`
- Android tests: Uses Espresso and AndroidX Test
- iOS tests: Through Xcode testing framework