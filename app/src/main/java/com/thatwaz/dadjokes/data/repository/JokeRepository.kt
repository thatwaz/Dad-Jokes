package com.thatwaz.dadjokes.data.repository

import com.thatwaz.dadjokes.data.api.JokeApiService
import com.thatwaz.dadjokes.data.db.JokeDao
import com.thatwaz.dadjokes.domain.model.Joke
import com.thatwaz.dadjokes.domain.model.toEntity
import com.thatwaz.dadjokes.domain.model.toJoke
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class JokeRepository @Inject constructor(
    private val api: JokeApiService,
    private val dao: JokeDao,
    private val cachedRepo: CachedJokesRepo
) {
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefetch = PrefetchJokesManager(api, repoScope)

    suspend fun getJoke(): Joke {
        return runCatching {
            val joke = prefetch.next()   // 🚀 fast path (memory) or batched fetch
            // optional: short-lived cache for offline UX
            cachedRepo.insert(
                setup = joke.setup,
                punch = joke.punchline,
                apiId = joke.id,   // JokeAPI numeric id
                hash  = stableJokeId(joke.setup, joke.punchline, joke.id),
                type  = joke.type
            )
            joke
        }.getOrElse {
            // Offline fallback (unchanged)
            val twoWeeksAgo = System.currentTimeMillis() - 14L * 24 * 60 * 60 * 1000
            val cached = cachedRepo.pickUnseen(twoWeeksAgo) ?: cachedRepo.pickAny()
            ?: error("No cached jokes available yet — open online once to prefill cache")
            Joke(
                id = cached.apiId ?: cached.hash.hashCode(),
                type = cached.type ?: "cached",
                setup = cached.setup,
                punchline = cached.punchline,
                rating = 0,
                isFavorite = false
            )
        }
    }

    private fun stableJokeId(setup: String, punch: String, externalId: Any?): String {
        val raw = "${externalId?.toString()?.trim() ?: ""}|${setup.trim()}|${punch.trim()}".lowercase()
        return raw.hashCode().toString()
    }


    suspend fun saveRating(joke: Joke) {
        dao.upsert(joke.toEntity())
    }

    fun getFavorites(): Flow<List<Joke>> {
        return dao.getFavoriteJokes().map { list -> list.map { it.toJoke() } }
    }

    fun getRatedJokes(): Flow<List<Joke>> =
        dao.getRatedJokes().map { list -> list.map { it.toJoke() } }

    suspend fun deleteRating(joke: Joke) {
        dao.clearRatingById(joke.id)
    }

    suspend fun clearAllRatings() {
        dao.clearAllRatings()
    }
}


