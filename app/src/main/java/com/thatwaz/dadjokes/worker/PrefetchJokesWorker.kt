package com.thatwaz.dadjokes.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.thatwaz.dadjokes.data.repository.CachedJokesRepo
import com.thatwaz.dadjokes.data.repository.JokeRepository
import com.thatwaz.dadjokes.data.repository.SeenJokesRepo
import com.thatwaz.dadjokes.ui.util.stableJokeId

import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class PrefetchJokesWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val cached: CachedJokesRepo
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // Belt & suspenders: if someone accidentally enqueues this, do no network work.
        // (We promised one request per user tap.)
        // Perform only cache maintenance/pruning.
        pruneOldCache()
        return Result.success()
    }

    private suspend fun pruneOldCache() {
        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        cached.dropOlderThan(thirtyDaysAgo)
    }
}


//@HiltWorker
//class PrefetchJokesWorker @AssistedInject constructor(
//    @Assisted appContext: Context,
//    @Assisted params: WorkerParameters,
//    private val repo: JokeRepository,
//    private val cached: CachedJokesRepo,
//    private val seen: SeenJokesRepo
//) : CoroutineWorker(appContext, params) {
//
//
//    override suspend fun doWork(): Result {
//        val target = 40
//        repeat(target) {
//            runCatching {
//                val j = repo.getJoke()
//                val hash = stableJokeId(j.setup, j.punchline, j.id.toString())
//                cached.insert(j.setup, j.punchline, j.id, hash, j.type ?: "cached")
//            }
//        }
//
//        // prune anything older than 30 days
//        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
//        cached.dropOlderThan(thirtyDaysAgo)
//
//        // (optional) log current cache size
//        // Log.d("Prefetch", "Cache size: ${cached.count()}")
//
//        return Result.success()
//    }
//
//
//
//
//}
