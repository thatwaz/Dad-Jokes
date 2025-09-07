package com.thatwaz.dadjokes.ui.common


import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

fun openCustomTab(context: Context, url: String) {
    try {
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(context, Uri.parse(url))
    } catch (_: ActivityNotFoundException) {
        // Fallback to default browser
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
