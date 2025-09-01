package com.thatwaz.dadjokes.ui.ads

import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAdSimple(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111", // TEST banner
    onLoaded: () -> Unit = {},
    onFailed: (LoadAdError) -> Unit = {},
    onClicked: () -> Unit = {}
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                adListener = object : AdListener() {
                    override fun onAdLoaded() = onLoaded()
                    override fun onAdFailedToLoad(error: LoadAdError) = onFailed(error)
                    override fun onAdClicked() = onClicked()
                }
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}











