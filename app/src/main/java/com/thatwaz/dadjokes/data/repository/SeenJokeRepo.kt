package com.thatwaz.dadjokes.data.repository



import com.thatwaz.dadjokes.data.db.SeenJoke
import com.thatwaz.dadjokes.data.db.SeenJokeDao
import javax.inject.Inject

class SeenJokesRepo @Inject constructor(
    private val dao: SeenJokeDao
) {
    suspend fun wasSeenWithin(id: String, ttlMillis: Long) =
        dao.existsWithin(id, System.currentTimeMillis() - ttlMillis)

    suspend fun markSeen(id: String, keep: Int) {
        dao.upsert(SeenJoke(id))
        dao.trimTo(keep)
    }

    suspend fun purgeOlderThan(ttlMillis: Long) {
        dao.dropOlderThan(System.currentTimeMillis() - ttlMillis)
    }
}
