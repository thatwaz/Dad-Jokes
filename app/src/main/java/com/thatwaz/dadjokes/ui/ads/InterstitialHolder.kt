package com.thatwaz.dadjokes.ui.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

private const val TAG = "InterstitialHolder"

/**
 * Lightweight interstitial manager with simple event callbacks.
 * Use Google TEST unit in dev: ca-app-pub-3940256099942544/1033173712
 */
class InterstitialHolder(
    private val context: Context,
    private val adUnitId: String = "ca-app-pub-3940256099942544/1033173712"
) {
    private var ad: InterstitialAd? = null

    // Event callbacks (set these from your Composable if you want reactions)
    var onReady: () -> Unit = {}
    var onShown: () -> Unit = {}
    var onDismissed: () -> Unit = {}
    var onLoadFailed: (LoadAdError) -> Unit = {}

    fun load() {
        InterstitialAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    ad = interstitialAd
                    Log.d(TAG, "Loaded")
                    onReady()
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    ad = null
                    Log.w(TAG, "Failed to load: $error")
                    onLoadFailed(error)
                }
            }
        )
    }

    fun isReady(): Boolean = ad != null

    /**
     * Shows the ad if ready. Calls [afterDismiss] once the ad is closed or fails to show.
     */
    fun show(activity: Activity, afterDismiss: () -> Unit) {
        val current = ad ?: run { afterDismiss(); return }

        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Shown")
                onShown()
            }
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Dismissed")
                ad = null
                load()              // Preload next
                onDismissed()
                afterDismiss()
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                Log.w(TAG, "Failed to show: $e")
                ad = null
                load()
                afterDismiss()
            }
        }

        // Make sure system bars are visible so the ad's own close affordance isn't obscured
        ensureSystemBarsVisible(activity)

        // Don’t force immersive; let the SDK handle its own UI
        current.setImmersiveMode(false)
        current.show(activity)
    }

    private fun ensureSystemBarsVisible(activity: Activity) {
        val window = activity.window
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.show(
            WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
        )
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    }
}

