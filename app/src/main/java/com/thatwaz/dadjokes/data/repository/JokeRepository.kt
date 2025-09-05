package com.thatwaz.dadjokes.data.repository

import com.thatwaz.dadjokes.data.api.JokeApiService
import com.thatwaz.dadjokes.data.api.toJoke
import com.thatwaz.dadjokes.data.db.JokeDao
import com.thatwaz.dadjokes.domain.model.Joke
import com.thatwaz.dadjokes.domain.model.toEntity
import com.thatwaz.dadjokes.domain.model.toJoke
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


class JokeRepository @Inject constructor(
    private val api: JokeApiService,
    private val dao: JokeDao,
    private val cachedRepo: CachedJokesRepo
) {
//    suspend fun getJoke(): Joke {
//        val jokeDto = api.getRandomJoke()
//        val cached = dao.getJokeById(jokeDto.id.toString())
//        return cached?.toJoke() ?: jokeDto.toJoke()
//    }

    suspend fun getJoke(): Joke {
        return runCatching {
            // Network first
            val dto = api.getRandomJoke()
            val joke = dto.toJoke() // gives you id:Int, type:String("single"/"twopart"), setup, punchline

            val hash = stableJokeId(
                setup = joke.setup,
                punch = joke.punchline,
                externalId = dto.id        // dto.id is String
            )

            // cache success
            cachedRepo.insert(
                setup = joke.setup,
                punch = joke.punchline,
                apiId = joke.id,           // Int from domain
                hash  = hash,
                type  = joke.type          // "single"/"twopart"
            )

            joke
        }.getOrElse {
            // Offline / network failure -> serve from cache
            val twoWeeksAgo = System.currentTimeMillis() - 14L * 24 * 60 * 60 * 1000
            val cached = cachedRepo.pickUnseen(twoWeeksAgo) ?: cachedRepo.pickAny()
            ?: error("No cached jokes available yet — open online once to prefill cache")

            // Rebuild the domain object from cache (include all required params!)
            Joke(
                id        = cached.apiId ?: cached.hash.hashCode(),
                type      = cached.type ?: "cached",
                setup     = cached.setup,
                punchline = cached.punchline,
                rating    = 0,
                isFavorite = false
            )
        }
    }

    private fun stableJokeId(setup: String, punch: String, externalId: String?): String {
        val raw = "${externalId?.trim() ?: ""}|${setup.trim()}|${punch.trim()}".lowercase()
        return raw.hashCode().toString()
    }



    suspend fun saveRating(joke: Joke) {
        dao.upsert(joke.toEntity())
    }
    fun getFavorites(): Flow<List<Joke>> {
        return dao.getFavoriteJokes().map { list -> list.map { it.toJoke() } }
    }

    // This is a suspend function you call in a blocking way from the worker
    fun getAllJokesBlocking(): List<Joke> {
        return runBlocking {
            dao.getAllJokesNow().map { it.toJoke() }
        }
    }

    fun getRatedJokes(): Flow<List<Joke>> =
        dao.getRatedJokes().map { list -> list.map { it.toJoke() } }





}

