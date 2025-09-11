package com.thatwaz.dadjokes

// Hilt ↔ WorkManager

// WorkManager APIs

// Your worker (use your actual package)

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.thatwaz.dadjokes.core.LicenseFlags
import com.thatwaz.dadjokes.worker.PrefetchJokesWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject
// App/DadJokesApp.kt
@HiltAndroidApp
class DadJokesApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // --- Ads init (only if allowed) ---
        if (LicenseFlags.ALLOW_MONETIZATION) {
            MobileAds.initialize(this) {}
            val testIds = listOf("ABCDEF012345") // TODO: replace with your real test device ID(s)
            val config = RequestConfiguration.Builder()
                .setTestDeviceIds(testIds)
                .build()
            MobileAds.setRequestConfiguration(config)
        }

        // --- Background prefetch (DISABLED unless both flags are true) ---
        if (LicenseFlags.ALLOW_API_USE && LicenseFlags.ALLOW_BACKGROUND_PREFETCH) {
            // Periodic (Wi-Fi + charging)
            val periodic = PeriodicWorkRequestBuilder<PrefetchJokesWorker>(12, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED) // Wi-Fi only
                        .setRequiresCharging(true)                     // while charging
                        .build()
                )
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "prefetchJokes",
                ExistingPeriodicWorkPolicy.KEEP,
                periodic
            )

            // One-time warmup prefetch (ASAP on any network)
            val warmup = OneTimeWorkRequestBuilder<PrefetchJokesWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(this).enqueueUniqueWork(
                "prefetchJokesWarmup",
                ExistingWorkPolicy.KEEP,
                warmup
            )
        }
    }
}

//@HiltAndroidApp
//class DadJokesApp : Application(), Configuration.Provider {
//
//    @Inject lateinit var workerFactory: HiltWorkerFactory
//
//    // WorkManager 2.8+ property-style override
//    override val workManagerConfiguration: Configuration
//        get() = Configuration.Builder()
//            .setWorkerFactory(workerFactory)
//            .build()
//
//    override fun onCreate() {
//        super.onCreate()
//
//        // --- Ads init ---
//        MobileAds.initialize(this) {}
//        val testIds = listOf("ABCDEF012345") // replace with your real test device ID
//        val config = RequestConfiguration.Builder()
//            .setTestDeviceIds(testIds)
//            .build()
//        MobileAds.setRequestConfiguration(config)
//
//        // --- Periodic cache prefetch (Wi-Fi + charging) ---
//        val periodic = PeriodicWorkRequestBuilder<PrefetchJokesWorker>(12, TimeUnit.HOURS)
//            .setConstraints(
//                Constraints.Builder()
//                    .setRequiredNetworkType(NetworkType.UNMETERED) // Wi-Fi only
//                    .setRequiresCharging(true)                     // while charging
//                    .build()
//            )
//            .build()
//
//        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
//            "prefetchJokes",
//            ExistingPeriodicWorkPolicy.KEEP,
//            periodic
//        )
//
//        // --- One-time warmup prefetch (run ASAP on any network) ---
//        val warmup = OneTimeWorkRequestBuilder<PrefetchJokesWorker>()
//            .setConstraints(
//                Constraints.Builder()
//                    .setRequiredNetworkType(NetworkType.CONNECTED)
//                    .build()
//            )
//            .build()
//
//        WorkManager.getInstance(this).enqueueUniqueWork(
//            "prefetchJokesWarmup",
//            ExistingWorkPolicy.KEEP,
//            warmup
//        )
//    }
//}
//
//
//
