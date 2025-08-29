package com.thatwaz.dadjokes.data.repository

import com.thatwaz.dadjokes.data.db.SavedJokeDao
import com.thatwaz.dadjokes.data.db.SavedJokeEntity
import com.thatwaz.dadjokes.data.db.toDomain
import com.thatwaz.dadjokes.domain.model.PersonSummary
import com.thatwaz.dadjokes.domain.model.SavedJokeDelivery
import com.thatwaz.dadjokes.domain.model.toDomain
import com.thatwaz.dadjokes.domain.model.toEntity
import com.thatwaz.dadjokes.domain.repository.SavedJokeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SavedJokeRepositoryImpl @Inject constructor(
    private val dao: SavedJokeDao
) : SavedJokeRepository {

    override suspend fun saveJoke(joke: SavedJokeDelivery) {
        dao.insertSavedJoke(joke.toEntity())
    }

    override suspend fun markAsTold(jokeId: Int, reaction: Int) {
        dao.markJokeAsTold(jokeId, reaction)
    }

    override suspend fun deleteJoke(joke: SavedJokeDelivery) {
        dao.deleteSavedJoke(joke.toEntity())
    }

    override fun getUntoldJokes(): Flow<List<SavedJokeDelivery>> {
        return dao.getUntoldJokes().map { it.map { entity -> entity.toDomain() } }
    }

    override fun getToldJokes(): Flow<List<SavedJokeDelivery>> {
        return dao.getToldJokes().map { it.map { entity -> entity.toDomain() } }
    }

    override fun getJokesForPerson(name: String): Flow<List<SavedJokeDelivery>> {
        return dao.getJokesForPerson(name).map { it.map { entity -> entity.toDomain() } }
    }


    override fun getPeople(): Flow<List<PersonSummary>> =
        dao.getPeopleSummary().map { it.map { row -> row.toDomain() } }

    override fun getUntoldForPerson(name: String): Flow<List<SavedJokeDelivery>> =
        dao.getUntoldForPerson(name).map { it.map { e -> e.toDomain() } }

    override fun getToldForPerson(name: String): Flow<List<SavedJokeDelivery>> =
        dao.getToldForPerson(name).map { it.map { e -> e.toDomain() } }

    override suspend fun saveJokeToPeople(setup: String, punchline: String, people: List<String>) {
        people.forEach { person ->
            dao.insertSavedJoke(
                SavedJokeEntity(
                    setup = setup,
                    punchline = punchline,
                    personName = person
                )
            )
        }
    }

    // SavedJokeRepositoryImpl.kt
    override suspend fun deleteAllForPerson(name: String) = dao.deleteAllForPerson(name)
    override suspend fun deleteAllSaved() = dao.deleteAllSaved()
    override suspend fun markAsUntold(id: Int) = dao.markUntold(id)


}
