# iOS Archive Build Instructions for PiGenerator2

## Prerequisites

1. **Xcode** - Make sure you have Xcode installed and updated
2. **Apple Developer Account** - You need a paid Apple Developer account to upload to App Store
3. **Certificates and Provisioning Profiles** - These should be set up in your Apple Developer account

## Step-by-Step Instructions

### 1. Prepare the Build Environment

```bash
# Navigate to the iOS project directory
cd /Users/markduenas/development/code/PiGenerator2/iosApp

# Install/update CocoaPods dependencies
pod install

# Ensure Gradle dependencies are built
cd .. && ./gradlew :composeApp:podInstallSyntheticIos
```

### 2. Open Xcode Workspace

```bash
# Open the workspace (NOT the .xcodeproj file)
open iosApp.xcworkspace
```

**Important**: Always use `iosApp.xcworkspace` and never `iosApp.xcodeproj` because of CocoaPods integration.

### 3. Configure Build Settings in Xcode

1. **Select the iosApp project** in the project navigator
2. **Select the iosApp target**
3. **Go to "Signing & Capabilities" tab**
4. **Verify the following settings**:
   - Bundle Identifier: `com.markduenas.android.apigen`
   - Team: Select your Apple Developer team
   - Provisioning Profile: Automatic or select appropriate profile
   - Signing Certificate: Distribution certificate for release builds

### 4. Build Configuration

1. **Select "Any iOS Device" from the device dropdown** (next to the scheme selector)
2. **Ensure the scheme is set to "iosApp"**
3. **Set build configuration to "Release"**:
   - Product → Scheme → Edit Scheme
   - In the Archive section, change Build Configuration to "Release"

### 5. Create Archive

**Method 1: Using Xcode Menu (Recommended)**
1. Go to **Product → Archive**
2. Wait for the build to complete (this may take several minutes)
3. The Organizer window will open automatically when done

**Method 2: Using Command Line**
```bash
cd /Users/markduenas/development/code/PiGenerator2/iosApp

# Create archive with proper destination
xcodebuild -workspace iosApp.xcworkspace \
           -scheme iosApp \
           -configuration Release \
           -destination "generic/platform=iOS" \
           -archivePath "build/PiGenerator.xcarchive" \
           archive
```

### 6. Validate and Upload

Once the archive is created:

1. **In Xcode Organizer**:
   - Your archive will appear in the Archives tab
   - Select your archive
   - Click **"Validate App"** to check for issues
   - If validation passes, click **"Distribute App"**

2. **Distribution Options**:
   - **App Store Connect**: For App Store submission
   - **Ad Hoc**: For testing on specific devices
   - **Enterprise**: For enterprise distribution
   - **Development**: For development testing

3. **For App Store submission**:
   - Choose "App Store Connect"
   - Select your distribution certificate
   - Choose "Upload" (not "Export")
   - Follow the prompts to upload

### 7. App Store Connect

After successful upload:

1. Go to [App Store Connect](https://appstoreconnect.apple.com)
2. Navigate to your app
3. The new build will appear in the "Build" section (may take a few minutes)
4. You can then submit for review or use for TestFlight

## Troubleshooting

### Common Issues and Solutions

**1. Build Errors**
```bash
# Clean build if you encounter issues
cd /Users/markduenas/development/code/PiGenerator2/iosApp
xcodebuild -workspace iosApp.xcworkspace -scheme iosApp clean
```

**2. CocoaPods Issues**
```bash
# Update CocoaPods
cd /Users/markduenas/development/code/PiGenerator2/iosApp
pod install --repo-update
```

**3. Kotlin Framework Issues**
```bash
# Rebuild Kotlin framework
cd /Users/markduenas/development/code/PiGenerator2
./gradlew :composeApp:podInstallSyntheticIos
```

**4. Provisioning Profile Issues**
- Check that your bundle identifier matches your app ID
- Ensure your provisioning profile includes the device/distribution
- Verify your certificates are valid and not expired

**5. AdMob Integration Issues**
- Ensure `Google-Mobile-Ads-SDK` pod is installed
- Check that `AdMobBannerViewController.swift` is included in the project
- Verify the Swift bridge is properly registered in `iOSApp.swift`

### Build Settings Verification

Before archiving, verify these settings in Xcode:

1. **Bundle Identifier**: `com.markduenas.android.apigen`
2. **Version**: Update as needed (CFBundleShortVersionString)
3. **Build Number**: Must be unique for each upload (CFBundleVersion)
4. **Deployment Target**: iOS 12.0 (as specified in your project)
5. **Architectures**: arm64 (for device builds)

### File Structure Check

Ensure these files exist and are properly configured:

- `iosApp/iosApp.xcworkspace` ✓
- `iosApp/Podfile` ✓
- `iosApp/Podfile.lock` ✓
- `iosApp/iosApp/iOSApp.swift` ✓
- `iosApp/iosApp/AdMobBannerViewController.swift` ✓
- `iosApp/iosApp/Info.plist` ✓

## Archive Location

If using command line, the archive will be created at:
```
/Users/markduenas/development/code/PiGenerator2/iosApp/build/PiGenerator.xcarchive
```

If using Xcode, archives are stored in:
```
~/Library/Developer/Xcode/Archives/
```

## Next Steps After Archive

1. **Upload to App Store Connect**
2. **Test with TestFlight** (optional but recommended)
3. **Submit for App Store Review**
4. **Monitor review status** in App Store Connect

## Tips

- **Always test on a physical device** before archiving
- **Use TestFlight** for beta testing before public release
- **Check App Store Review Guidelines** before submission
- **Keep your certificates and profiles updated**
- **Increment build numbers** for each upload

## Support

If you encounter issues:
1. Check the Xcode console for detailed error messages
2. Verify your Apple Developer account status
3. Ensure all certificates and profiles are valid
4. Check the AdMob integration following the provided documentation