package com.thatwaz.dadjokes.ui.ads



enum class AdFlow {
    Idle,            // normal jokes UI
    PreRoll,         // “ad incoming” big theater screen
    ShowingAd,       // interstitial currently showing
    PostRoll         // “ad just ended” big theater screen
}
