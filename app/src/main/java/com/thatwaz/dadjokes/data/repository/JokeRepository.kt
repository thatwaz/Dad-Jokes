package com.thatwaz.dadjokes.data.repository

import com.thatwaz.dadjokes.data.api.JokeApiService
import com.thatwaz.dadjokes.data.db.JokeDao
import com.thatwaz.dadjokes.domain.model.Joke
import com.thatwaz.dadjokes.domain.model.toEntity
import com.thatwaz.dadjokes.domain.model.toJoke
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import com.thatwaz.dadjokes.data.api.toJoke



class JokeRepository @Inject constructor(
    private val api: JokeApiService,
    private val dao: JokeDao
) {
    suspend fun getJoke(): Joke {
        val jokeDto = api.getRandomJoke()
        val cached = dao.getJokeById(jokeDto.id.toString())
        return cached?.toJoke() ?: jokeDto.toJoke()
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

}

