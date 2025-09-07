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

    // Event callbacks (optional)
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

        // Make sure bars are visible BEFORE showing the ad
        ensureSystemBarsVisible(activity)

        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Shown")
                onShown()
            }
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Dismissed")
                ad = null
                load() // preload next

                // Make sure bars are visible AFTER the ad too
                ensureSystemBarsVisible(activity)

                onDismissed()
                afterDismiss()
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                Log.w(TAG, "Failed to show: $e")
                ad = null
                load()

                // If it failed to show, also ensure bars are visible
                ensureSystemBarsVisible(activity)

                afterDismiss()
            }
        }

        // Don’t force immersive; let the SDK manage its UI
        current.setImmersiveMode(false)
        current.show(activity)
    }

    private fun ensureSystemBarsVisible(activity: Activity) {
        val w = activity.window

        // Lay out below the bars (keeps status/navigation visible)
        WindowCompat.setDecorFitsSystemWindows(w, true)

        // Explicitly show them
        WindowInsetsControllerCompat(w, w.decorView).apply {
            show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }
    }


}


