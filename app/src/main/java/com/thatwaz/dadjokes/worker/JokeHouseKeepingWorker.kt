package com.thatwaz.dadjokes.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.thatwaz.dadjokes.data.repository.SeenJokesRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class JokeHousekeepingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val seenRepo: SeenJokesRepo
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val ttlMillis = 14L * 24 * 60 * 60 * 1000 // keep “seen” for 14 days
        return try {
            seenRepo.purgeOlderThan(ttlMillis)
            Result.success()
        } catch (_: Throwable) {
            // If DB is temporarily locked, try again later
            Result.retry()
        }
    }
}
