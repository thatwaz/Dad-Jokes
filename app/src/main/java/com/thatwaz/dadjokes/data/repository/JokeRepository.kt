package com.thatwaz.dadjokes.data.repository

import com.thatwaz.dadjokes.data.api.JokeApiService
import com.thatwaz.dadjokes.data.db.JokeDao
import com.thatwaz.dadjokes.domain.model.Joke
import com.thatwaz.dadjokes.domain.model.toEntity
import com.thatwaz.dadjokes.domain.model.toJoke
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class JokeRepository @Inject constructor(
    private val api: JokeApiService,
    private val dao: JokeDao
) {
    suspend fun getJoke(): Joke {
        val jokeDto = api.getRandomJoke()
        val cached = dao.getJokeById(jokeDto.id)
        return cached?.toJoke() ?: jokeDto.toJoke()
    }

    suspend fun saveRating(joke: Joke) {
        dao.upsert(joke.toEntity())
    }
    fun getFavorites(): Flow<List<Joke>> {
        return dao.getFavoriteJokes().map { list -> list.map { it.toJoke() } }
    }



}

