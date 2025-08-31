package com.thatwaz.dadjokes.ui.ads



import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialHolder(
    private val context: Context,
    private val adUnitId: String = "ca-app-pub-3940256099942544/1033173712" // Google TEST interstitial
) {
    private var ad: InterstitialAd? = null

    fun load() {
        InterstitialAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) { ad = interstitialAd }
                override fun onAdFailedToLoad(error: LoadAdError) { ad = null }
            }
        )
    }

    fun isReady(): Boolean = ad != null

    // InterstitialHolder.kt

    fun show(activity: Activity, afterDismiss: () -> Unit) {
        val current = ad ?: return afterDismiss()
        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                // optional: log
            }
            override fun onAdDismissedFullScreenContent() {
                ad = null
                load()
                afterDismiss()
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                ad = null
                load()
                afterDismiss()
            }
        }
        current.setImmersiveMode(false)         // don’t hide OS chrome
        ensureSystemBarsVisible(activity)       // make bars visible right now
        current.show(activity)
    }

}
