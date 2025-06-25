# Pi Generator KMP Compose Mobile App Specification

## Overview
A cross-platform mobile application built with Kotlin Multiplatform (KMP) and Compose Multiplatform that combines pi calculation, memorization training, and pattern exploration features with integrated mobile advertising through AdMob.

## Target Platforms
- Android (API 24+)
- iOS (iOS 13.0+)

## Core Features

### 1. Multi-Algorithm Pi Calculator
**Description**: High-precision pi calculation with multiple algorithms
**User Value**: Flexibility and educational insight into different mathematical approaches

**Features**:
- **AGM + FFT Algorithm**: Fastest calculation method for large digit counts
- **Machin's Formula**: Real-time digit accumulation visualization (educational)
- **Spigot Algorithm**: Memory-efficient calculation for specific digit ranges
- **Custom Precision**: User-selectable digit count (up to 1 million digits)
- **Real-time Progress**: Live calculation progress with estimated completion time
- **Algorithm Comparison**: Side-by-side performance benchmarking

**UI Components**:
- Algorithm selection dropdown
- Precision input slider/field
- Start/Stop calculation buttons
- Progress indicator with percentage and ETA
- Results display with scrollable digit viewer

### 2. Pi Memorization Training Game
**Description**: Gamified pi digit memorization with multiple learning aids
**User Value**: Proven effective method for memorizing hundreds of pi digits

**Features**:
- **Progressive Learning**: Start with 10 digits, unlock more as you progress
- **Multiple Input Modes**: 
  - Touch typing on custom numeric keypad
  - Voice recognition for hands-free practice
  - Gesture-based input for advanced users
- **Memory Aids**:
  - Color coding for digit patterns
  - Musical note associations for each digit (0-9)
  - Visual chunking (groups of 5 or 10 digits)
- **Statistics Tracking**:
  - Personal best records
  - Learning velocity (digits per day)
  - Accuracy percentages
  - Time-based challenges
- **Achievement System**: Badges for milestones (50, 100, 500, 1000 digits)

**UI Components**:
- Practice mode interface with hints
- Test mode with timer
- Statistics dashboard with charts
- Achievement gallery
- Leaderboard integration

### 3. Pi Pattern Search & Explorer
**Description**: Interactive tool for finding meaningful patterns within pi digits
**User Value**: Personal connection and curiosity satisfaction

**Features**:
- **Personal Pattern Search**:
  - Birthday finder
  - Phone number locator
  - Custom sequence search
- **Famous Sequences**:
  - Feynman Point (999999 sequence)
  - Other mathematical curiosities
  - Historical significance explanations
- **Pattern Statistics**:
  - Frequency analysis of digit combinations
  - Pattern occurrence probability
  - Distance between pattern matches
- **Visual Exploration**:
  - Digit sequence visualization
  - Pattern highlighting in calculated results
  - Shareable pattern discoveries

**UI Components**:
- Search input with pattern type selection
- Results list with position indicators
- Visual digit browser with highlighting
- Pattern statistics cards
- Social sharing integration

## Technical Architecture

### KMP Structure
```
shared/
├── commonMain/
│   ├── kotlin/
│   │   ├── calculation/     # Pi algorithms
│   │   ├── memorization/    # Game logic
│   │   ├── patterns/        # Search functionality
│   │   ├── data/           # Data models
│   │   └── ui/             # Shared UI components
├── androidMain/
│   └── kotlin/             # Android-specific implementations
└── iosMain/
    └── kotlin/             # iOS-specific implementations

androidApp/
├── src/main/kotlin/        # Android app entry point
└── src/main/res/           # Android resources

iosApp/
└── iosApp/                 # iOS app entry point
```

### Key Dependencies
- **Kotlin Multiplatform**: 1.9.20+
- **Compose Multiplatform**: 1.5.4+
- **Ktor**: Network operations
- **SQLDelight**: Local database
- **Kotlinx.serialization**: Data serialization
- **Kotlinx.coroutines**: Async operations

## Mobile Advertising Integration

### AdMob Implementation
**Strategy**: Non-intrusive ad placement to maintain user experience quality

**Ad Placements**:
1. **Banner Ads**:
   - Bottom of calculation results screen
   - Below memorization game statistics
   - Footer of pattern search results

2. **Interstitial Ads**:
   - Between memorization game levels (every 5 levels)
   - After completing major calculations (>100k digits)
   - When switching between major app sections

3. **Rewarded Video Ads**:
   - Unlock premium algorithms early
   - Get hints during memorization challenges
   - Remove ads for 24 hours

**Implementation Details**:
- **Android**: Google Mobile Ads SDK
- **iOS**: Google Mobile Ads SDK for iOS
- **KMP Integration**: Expect/actual pattern for platform-specific ad calls
- **Ad Frequency**: Maximum 3 ads per 10-minute session
- **Ad-Free Option**: Premium upgrade available via in-app purchase

### Ad Integration Code Structure
```kotlin
// Shared interface
expect class AdManager {
    fun loadBannerAd(adUnitId: String)
    fun showInterstitialAd(callback: (Boolean) -> Unit)
    fun showRewardedAd(callback: (Boolean) -> Unit)
}

// Platform-specific implementations
// Android: Uses Google Mobile Ads SDK
// iOS: Uses Google Mobile Ads SDK for iOS
```

## User Experience Design

### Navigation Structure
- **Bottom Navigation**: Calculator, Training, Explorer
- **Top App Bar**: Settings, Statistics, Share
- **Floating Action Button**: Quick calculation start

### Theme Support
- Light/Dark mode toggle
- High contrast mode for accessibility
- Custom color schemes for memorization training

### Accessibility
- Screen reader support
- Large text options
- Voice input/output capabilities
- High contrast patterns for visual impairments

## Performance Requirements
- **Calculation Performance**: Handle up to 1M digits within 30 seconds on mid-range devices
- **Memory Usage**: Maximum 100MB during intensive calculations
- **Battery Optimization**: Background calculation suspension
- **Startup Time**: <2 seconds cold start

## Monetization Strategy
1. **Free Tier**: 
   - Basic algorithms (Machin's formula)
   - 100-digit memorization limit
   - Standard pattern search
   - Ad-supported

2. **Premium Tier** ($4.99):
   - All algorithms including AGM+FFT
   - Unlimited digit memorization
   - Advanced pattern statistics
   - Ad-free experience
   - Cloud sync for progress

## Development Phases

### Phase 1 (MVP - 8 weeks)
- Basic pi calculation (Machin's algorithm)
- Simple memorization game (up to 100 digits)
- Basic pattern search
- AdMob integration

### Phase 2 (Enhancement - 4 weeks)
- Additional algorithms (AGM+FFT)
- Advanced memorization features
- Enhanced pattern exploration
- Performance optimizations

### Phase 3 (Polish - 2 weeks)
- UI/UX refinements
- Premium feature implementation
- Analytics integration
- App store optimization

## Success Metrics
- **User Engagement**: Average session duration >5 minutes
- **Retention**: 30% 7-day retention rate
- **Monetization**: $1+ ARPU within 30 days
- **App Store Rating**: 4.5+ stars with 100+ reviews
- **Educational Impact**: Users memorizing 50+ digits within first week