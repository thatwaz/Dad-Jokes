package com.thatwaz.dadjokes.data.repository

//import com.thatwaz.dadjokes.data.api.JokeApiItem
//import com.thatwaz.dadjokes.data.api.JokeApiService
//import com.thatwaz.dadjokes.data.api.toJoke
//import com.thatwaz.dadjokes.domain.model.Joke
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.sync.Mutex
//import kotlinx.coroutines.sync.withLock
//import java.util.ArrayDeque
//import kotlin.math.min
//
///**
// * Efficient batched fetching for JokeAPI.
// * - Fetches up to 10 at once
// * - Keeps a small buffer
// * - Prefetches when low
// * - Dedupes by provider id
// */
//class PrefetchJokesManager(
//    private val api: JokeApiService,
//    private val scope: CoroutineScope,
//    private val categories: String = "Misc,Pun",
//    private val safeMode: Boolean = true
//) {
//    companion object {
//        private const val MAX_BATCH = 10              // JokeAPI cap
//        private const val TARGET_BUFFER = 20          // total we try to keep in memory
//        private const val PREFETCH_TRIGGER = 6        // when <= this, we top up
//        private const val MAX_SEEN_IDS = 1000         // LRU size for dedupe
//    }
//
//    private val queue = ArrayDeque<Joke>(TARGET_BUFFER)
//    private val seenIds = LruIntSet(MAX_SEEN_IDS)
//    private val mutex = Mutex()
//    @Volatile private var prefetchJob: Job? = null
//
//    suspend fun next(): Joke {
//        // Fast path: if available, pop without hitting network
//        mutex.withLock {
//            queue.pollFirst()?.let { j ->
//                maybePrefetchLocked()
//                return j
//            }
//        }
//        // Buffer empty: fetch synchronously
//        refillSync(min(MAX_BATCH, TARGET_BUFFER))
//        return mutex.withLock {
//            queue.pollFirst() ?: error("No jokes available after fetch")
//        }
//    }
//
//    private fun maybePrefetchLocked() {
//        if (queue.size <= PREFETCH_TRIGGER && (prefetchJob == null || prefetchJob?.isActive == false)) {
//            prefetchJob = scope.launch(Dispatchers.IO) { refillSync(MAX_BATCH) }
//        }
//    }
//
//    private suspend fun refillSync(request: Int) {
//        val amount = request.coerceIn(1, MAX_BATCH)
//        val resp = api.getJokes(
//            categories = categories,
//            type = "single,twopart",
//            amount = amount,
//            lang = "en",
//            blacklistFlags = "nsfw,religious,political,racist,sexist,explicit",
//            safeMode = if (safeMode) "1" else ""
//        )
//        val items = resp.jokes.orEmpty()
//
//        val mapped: List<Joke> = items
//            .asSequence()
//            .filter { it.id != null }                 // must have provider id
//            .filter { seenIds.add(it.id!!) }          // dedupe (true if newly added)
//            .mapNotNull(JokeApiItem::toJoke)
//            .toList()
//
//        mutex.withLock {
//            // top up to TARGET_BUFFER
//            val room = TARGET_BUFFER - queue.size
//            mapped.take(room).forEach(queue::addLast)
//            // If still low, kick another background fetch
//            maybePrefetchLocked()
//        }
//    }
//}
//
///** Fixed-size LRU-ish set for Int ids (very light). */
//private class LruIntSet(private val max: Int) {
//    private val order = ArrayDeque<Int>(max)
//    private val set = HashSet<Int>(max)
//
//    fun add(id: Int): Boolean {
//        if (set.contains(id)) return false
//        set.add(id)
//        order.addLast(id)
//        if (order.size > max) {
//            val old = order.removeFirst()
//            set.remove(old)
//        }
//        return true
//    }
//}
