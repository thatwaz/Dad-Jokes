package com.thatwaz.dadjokes



import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.thatwaz.dadjokes.worker.JokeHousekeepingWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class DadJokesApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // --- Ads init (unchanged) ---
        MobileAds.initialize(this) {}
        val testIds = listOf("ABCDEF012345") // replace with your real test device ID
        val config = RequestConfiguration.Builder()
            .setTestDeviceIds(testIds)
            .build()
        MobileAds.setRequestConfiguration(config)

        // --- Periodic housekeeping: purge old "seen" entries once/day ---
        val constraints = Constraints.Builder()
            // No network required anymore
            // Optionally: .setRequiresCharging(true) if you want to be extra battery friendly
            .build()

        val dailyHousekeeping = PeriodicWorkRequestBuilder<JokeHousekeepingWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "jokeHousekeeping",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyHousekeeping
        )

        // Removed: one-time and periodic PrefetchJokesWorker enqueues
    }
}



