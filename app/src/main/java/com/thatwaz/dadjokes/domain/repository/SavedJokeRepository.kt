package com.thatwaz.dadjokes.domain.repository

import com.thatwaz.dadjokes.domain.model.PersonSummary
import com.thatwaz.dadjokes.domain.model.SavedJokeDelivery
import kotlinx.coroutines.flow.Flow

interface SavedJokeRepository {
    suspend fun saveJoke(joke: SavedJokeDelivery)
    suspend fun markAsTold(jokeId: Int, reaction: Int)
    suspend fun deleteJoke(joke: SavedJokeDelivery)
    fun getUntoldJokes(): Flow<List<SavedJokeDelivery>>
    fun getToldJokes(): Flow<List<SavedJokeDelivery>>
    fun getJokesForPerson(name: String): Flow<List<SavedJokeDelivery>>

    // com.thatwaz.dadjokes.domain.repository.SavedJokeRepository.kt
    fun getPeople(): Flow<List<PersonSummary>>
    fun getUntoldForPerson(name: String): Flow<List<SavedJokeDelivery>>
    fun getToldForPerson(name: String): Flow<List<SavedJokeDelivery>>
    suspend fun saveJokeToPeople(setup: String, punchline: String, people: List<String>)

    // SavedJokeRepository.kt
    suspend fun deleteAllForPerson(name: String)
    suspend fun deleteAllSaved()

    suspend fun markAsUntold(id: Int)

}
