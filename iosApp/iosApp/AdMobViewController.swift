import UIKit
import GoogleMobileAds
import ComposeApp

/**
 * UIViewController that manages AdMob banner ads for integration with Compose Multiplatform
 */
class AdMobViewController: UIViewController {
    
    private var bannerView: BannerView!
    private let adUnitId: String
    
    init(adUnitId: String) {
        self.adUnitId = adUnitId
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
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
        // Create banner view with appropriate ad size
        bannerView = BannerView(adSize: AdSizeBanner)
        bannerView.adUnitID = adUnitId
        bannerView.rootViewController = self
        bannerView.delegate = self
        
        // Configure banner view
        bannerView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(bannerView)
        
        // Set up constraints to fill the view
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

// MARK: - GADBannerViewDelegate
extension AdMobViewController: BannerViewDelegate {
    
    func bannerViewDidReceiveAd(_ bannerView: BannerView) {
        print("AdMob: Banner ad loaded successfully")
    }
    
    func bannerView(_ bannerView: BannerView, didFailToReceiveAdWithError error: Error) {
        print("AdMob: Failed to load banner ad: \(error.localizedDescription)")
    }
    
    func bannerViewDidRecordImpression(_ bannerView: BannerView) {
        print("AdMob: Banner ad impression recorded")
    }
    
    func bannerViewWillPresentScreen(_ bannerView: BannerView) {
        print("AdMob: Banner ad will present screen")
    }
    
    func bannerViewWillDismissScreen(_ bannerView: BannerView) {
        print("AdMob: Banner ad will dismiss screen")
    }
    
    func bannerViewDidDismissScreen(_ bannerView: BannerView) {
        print("AdMob: Banner ad did dismiss screen")
    }
}