import SwiftUI
import GoogleMobileAds
import ComposeApp

@main
struct iOSApp: App {
    
    init() {
        MobileAds.shared.start(completionHandler: nil)
        
        // Set up the AdMob factory for Kotlin interop
        AdMobManager_iosKt.adMobViewControllerFactory = { adUnitId in
            return AdMobViewController(adUnitId: adUnitId)
        }
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
