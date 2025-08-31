package com.thatwaz.dadjokes

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DadJokesApp : Application() {
    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this) {}

        // Replace with the ID you saw in Logcat
        val testIds = listOf("ABCDEF012345")
        val config = RequestConfiguration.Builder()
            .setTestDeviceIds(testIds)
            .build()
        MobileAds.setRequestConfiguration(config)
    }
}
