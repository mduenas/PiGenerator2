import SwiftUI
import GoogleMobileAds
import StoreKit
import ComposeApp

@main
struct iOSApp: App {

    init() {
        // Initialize Google Mobile Ads
        MobileAds.shared.start(completionHandler: nil)

        // Set up the AdMob factory for Kotlin interop
        AdMobManager_iosKt.adMobViewControllerFactory = { adUnitId in
            return AdMobViewController(adUnitId: adUnitId)
        }

        // Initialize StoreKit manager for in-app purchases
        StoreKitManager.shared.initialize()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
