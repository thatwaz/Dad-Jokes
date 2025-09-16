package com.thatwaz.dadjokes.data.repository

import com.thatwaz.dadjokes.data.db.JokeDao
import com.thatwaz.dadjokes.domain.model.Joke
import com.thatwaz.dadjokes.domain.model.toEntity
import com.thatwaz.dadjokes.domain.model.toJoke
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JokeRepository @Inject constructor(
    private val dao: JokeDao
) {
    // Ratings / favorites only (local)
    suspend fun saveRating(joke: Joke) {
        dao.upsert(joke.toEntity())
    }

    fun getFavorites(): Flow<List<Joke>> =
        dao.getFavoriteJokes().map { list -> list.map { it.toJoke() } }

    fun getRatedJokes(): Flow<List<Joke>> =
        dao.getRatedJokes().map { list -> list.map { it.toJoke() } }

    suspend fun deleteRating(joke: Joke) {
        dao.clearRatingById(joke.id)
    }

    suspend fun clearAllRatings() {
        dao.clearAllRatings()
    }
}


