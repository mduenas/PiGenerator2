# AdMob Migration Plan: PiGenerator2 → HABa-Tracker Structure

## Overview

This document outlines the migration plan to restructure PiGenerator2's AdMob implementation to exactly match HABa-Tracker's proven architecture. The goal is to maintain identical implementations across both projects, differing only in app-specific identifiers.

## Current State Analysis

### HABa-Tracker Structure (Target)
```
composeApp/src/
├── commonMain/kotlin/com/markduenas/habatracker/
│   ├── config/
│   │   ├── AdMobConstants.kt           # All constants and ad unit IDs
│   │   └── BuildConfig.kt              # expect object
│   └── presentation/components/
│       ├── AdMobManager.kt             # expect class with BannerAdView
│       └── AdMobBanner.kt              # expect function
├── androidMain/kotlin/com/markduenas/habatracker/
│   ├── config/
│   │   └── BuildConfig.android.kt      # actual with context helpers
│   └── presentation/components/
│       ├── AdMobManager.android.kt     # actual implementation
│       └── AdMobBanner.android.kt      # actual function (direct AndroidView)
└── iosMain/kotlin/com/markduenas/habatracker/
    ├── config/
    │   └── BuildConfig.ios.kt          # actual implementation
    └── presentation/components/
        ├── AdMobManager.ios.kt         # actual with UIKitViewController
        └── AdMobBanner.ios.kt          # actual function (calls AdMobManager)
```

### PiGenerator2 Current Structure
```
composeApp/src/
├── commonMain/kotlin/com/markduenas/android/apigen/
│   ├── config/
│   │   ├── AdMobConfig.kt              # ❌ To be removed
│   │   └── BuildConfig.kt              # ✅ Keep (expect object)
│   ├── data/admob/
│   │   └── AdMobConstants.kt           # ❌ To be restructured
│   ├── ads/
│   │   └── AdMobManager.kt             # ❌ To be restructured
│   └── ui/components/
│       └── AdMobBanner.kt              # ❌ To be restructured
├── androidMain/kotlin/com/markduenas/android/apigen/
│   ├── config/
│   │   └── BuildConfig.android.kt      # ✅ Keep (needs context helpers)
│   └── ads/
│       └── AdMobManager.android.kt     # ❌ To be restructured
└── iosMain/kotlin/com/markduenas/android/apigen/
    ├── config/
    │   └── BuildConfig.ios.kt          # ✅ Keep
    └── ads/
        └── AdMobManager.ios.kt         # ❌ To be restructured
```

## Migration Steps

### Phase 1: File Structure Preparation

#### 1.1 Create New Directory Structure
```bash
mkdir -p composeApp/src/commonMain/kotlin/com/markduenas/android/apigen/presentation/components
mkdir -p composeApp/src/androidMain/kotlin/com/markduenas/android/apigen/presentation/components
mkdir -p composeApp/src/iosMain/kotlin/com/markduenas/android/apigen/presentation/components
```

#### 1.2 Files to Remove
- ❌ `composeApp/src/commonMain/kotlin/com/markduenas/android/apigen/config/AdMobConfig.kt`
- ❌ `composeApp/src/commonMain/kotlin/com/markduenas/android/apigen/data/admob/AdMobConstants.kt`
- ❌ `composeApp/src/commonMain/kotlin/com/markduenas/android/apigen/ads/AdMobManager.kt`
- ❌ `composeApp/src/commonMain/kotlin/com/markduenas/android/apigen/ui/components/AdMobBanner.kt`
- ❌ `composeApp/src/androidMain/kotlin/com/markduenas/android/apigen/ads/AdMobManager.android.kt`
- ❌ `composeApp/src/iosMain/kotlin/com/markduenas/android/apigen/ads/AdMobManager.ios.kt`

### Phase 2: Create New Files

#### 2.1 Common Files

**`composeApp/src/commonMain/kotlin/com/markduenas/android/apigen/config/AdMobConstants.kt`**
- Move from `data/admob/` to `config/`
- Remove `AdMobConfig` dependency
- Add hardcoded ad unit IDs (PiGenerator2 specific)
- Keep `getBannerAdUnitId()` function
- Add `ANDROID_APPLICATION_ID` and `IOS_APPLICATION_ID` constants

**`composeApp/src/commonMain/kotlin/com/markduenas/android/apigen/presentation/components/AdMobManager.kt`**
- Move from `ads/` to `presentation/components/`
- Simplify to match HABa-Tracker structure
- Remove `initialize()` and `isReady()` methods
- Keep only `BannerAdView` composable function
- Add `getAdMobManager()` expect function

**`composeApp/src/commonMain/kotlin/com/markduenas/android/apigen/presentation/components/AdMobBanner.kt`**
- Move from `ui/components/` to `presentation/components/`
- Change from wrapper class to expect/actual function
- Remove `GlobalAdMobManager` dependency

#### 2.2 Android Files

**`composeApp/src/androidMain/kotlin/com/markduenas/android/apigen/config/BuildConfig.android.kt`**
- Add context helper functions:
  - `setAndroidContext(context: Context)`
  - `getAndroidContext(): Context`
- Remove logging (keep clean like HABa-Tracker)

**`composeApp/src/androidMain/kotlin/com/markduenas/android/apigen/presentation/components/AdMobManager.android.kt`**
- Move from `ads/` to `presentation/components/`
- Simplify to match HABa-Tracker structure
- Remove complex initialization logic
- Remove logging and error handling
- Keep only basic `AdView` creation

**`composeApp/src/androidMain/kotlin/com/markduenas/android/apigen/presentation/components/AdMobBanner.android.kt`**
- Create new file
- Implement as actual function
- Direct `AndroidView` implementation (no AdMobManager dependency)

#### 2.3 iOS Files

**`composeApp/src/iosMain/kotlin/com/markduenas/android/apigen/presentation/components/AdMobManager.ios.kt`**
- Move from `ads/` to `presentation/components/`
- Simplify to match HABa-Tracker structure
- Remove complex initialization
- Keep UIKitViewController approach
- Add placeholder logic for debug mode

**`composeApp/src/iosMain/kotlin/com/markduenas/android/apigen/presentation/components/AdMobBanner.ios.kt`**
- Create new file
- Implement as actual function
- Call `getAdMobManager().BannerAdView()`

### Phase 3: Update App-Specific Configuration

#### 3.1 Ad Unit IDs (PiGenerator2 Specific)
Update `AdMobConstants.kt` with PiGenerator2's ad unit IDs:
```kotlin
const val ANDROID_BANNER_AD_UNIT_ID = "ca-app-pub-7540731406850248/6300978111"
const val IOS_BANNER_AD_UNIT_ID = "ca-app-pub-7540731406850248/7811996861"
const val ANDROID_APPLICATION_ID = "ca-app-pub-7540731406850248~8469817354"
const val IOS_APPLICATION_ID = "ca-app-pub-7540731406850248~1629731896"
```

#### 3.2 Remove Complex Debug/Production Logic
- Remove `AdMobConfig.TEST_MODE` logic
- Remove test ad unit switching
- Simplify to use production ad units always
- Debug mode only affects iOS placeholder display

### Phase 4: Update Usage Points

#### 4.1 Screen Updates
Update all screen files to use new import:
```kotlin
// OLD
import com.markduenas.android.apigen.ui.components.AdMobBanner

// NEW
import com.markduenas.android.apigen.presentation.components.AdMobBanner
```

#### 4.2 Remove MainActivity AdMob Initialization
- Remove `AndroidAdManager` import and initialization
- Remove `PlatformAdManager` references
- Android initialization will be handled automatically

### Phase 5: Cleanup

#### 5.1 Remove Unused Files
- Remove entire `ads/` directory
- Remove `data/admob/` directory
- Remove `ui/components/AdMobBanner.kt`
- Remove `config/AdMobConfig.kt`

#### 5.2 Update Imports
- Update all import statements across the codebase
- Remove references to removed classes

## Key Differences from HABa-Tracker

### App-Specific Identifiers
| Component | HABa-Tracker | PiGenerator2 |
|-----------|-------------|-------------|
| Package | `com.markduenas.habatracker` | `com.markduenas.android.apigen` |
| Android App ID | `ca-app-pub-7540731406850248~5361017320` | `ca-app-pub-7540731406850248~8469817354` |
| iOS App ID | `ca-app-pub-7540731406850248~1421772311` | `ca-app-pub-7540731406850248~1629731896` |
| Android Banner | `ca-app-pub-7540731406850248/2734853984` | `ca-app-pub-7540731406850248/6300978111` |
| iOS Banner | `ca-app-pub-7540731406850248/6482527304` | `ca-app-pub-7540731406850248/7811996861` |

### Context Helper Location
- HABa-Tracker: `BuildConfig.android.kt` (separate functions)
- PiGenerator2: Currently in `io/` package, move to `BuildConfig.android.kt`

## Implementation Order

1. **Backup Current Implementation**
   ```bash
   git add . && git commit -m "Backup before AdMob migration"
   ```

2. **Create New Directory Structure**
   - Create `presentation/components/` directories

3. **Create New CommonMain Files**
   - `config/AdMobConstants.kt` (simplified)
   - `presentation/components/AdMobManager.kt` (simplified)
   - `presentation/components/AdMobBanner.kt` (expect function)

4. **Create New Android Files**
   - Update `config/BuildConfig.android.kt` (add context helpers)
   - `presentation/components/AdMobManager.android.kt` (simplified)
   - `presentation/components/AdMobBanner.android.kt` (direct AndroidView)

5. **Create New iOS Files**
   - `presentation/components/AdMobManager.ios.kt` (simplified)
   - `presentation/components/AdMobBanner.ios.kt` (calls AdMobManager)

6. **Update Usage Points**
   - Update all screen imports
   - Remove MainActivity initialization

7. **Remove Old Files**
   - Remove old `ads/` directory
   - Remove old `data/admob/` directory
   - Remove old `ui/components/AdMobBanner.kt`
   - Remove `config/AdMobConfig.kt`

8. **Test Both Platforms**
   - Verify Android ads work
   - Verify iOS ads work
   - Verify debug mode placeholder on iOS

## Success Criteria

✅ **Structure Match**: File structure exactly matches HABa-Tracker
✅ **Functionality**: Ads display correctly on both platforms
✅ **Simplicity**: Removed complex initialization and debug logic
✅ **Maintainability**: Single source of truth for ad configuration
✅ **Consistency**: Implementation identical to HABa-Tracker (except IDs)

## Risk Mitigation

- **Git Backup**: Commit before starting migration
- **Incremental Testing**: Test after each major phase
- **Platform Testing**: Verify both Android and iOS work
- **Rollback Plan**: Keep old implementation in git history

## Post-Migration Verification

1. **Android Testing**
   - Run debug build: Should show real ads
   - Check logcat for any errors
   - Verify ad unit IDs are correct

2. **iOS Testing**
   - Run debug build: Should show placeholder
   - Run release build: Should show real ads
   - Verify Swift bridge works correctly

3. **Code Review**
   - Compare final structure with HABa-Tracker
   - Verify all imports are correct
   - Ensure no dead code remains

This migration will result in a cleaner, more maintainable AdMob implementation that exactly matches the proven HABa-Tracker architecture.