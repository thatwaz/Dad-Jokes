package com.thatwaz.dadjokes.data.repository

import com.thatwaz.dadjokes.data.db.CachedJokeDao
import com.thatwaz.dadjokes.data.db.CachedJokeEntity
import javax.inject.Inject

// com.thatwaz.dadjokes.data.repository.CachedJokesRepo
class CachedJokesRepo @Inject constructor(
    private val dao: CachedJokeDao
) {
    suspend fun insert(
        setup: String,
        punch: String,
        apiId: Int?,
        hash: String,
        type: String?            // 👈 add
    ) {
        dao.insertAll(
            listOf(
                CachedJokeEntity(
                    apiId = apiId,
                    type = type,    // 👈 pass it
                    setup = setup,
                    punchline = punch,
                    hash = hash
                )
            )
        )
    }

    suspend fun pickUnseen(cutoff: Long) = dao.pickUnseen(cutoff)
    suspend fun dropOlderThan(cutoffOld: Long) = dao.dropOlderThan(cutoffOld)
    suspend fun count() = dao.count()
    // Repo
    suspend fun pickAny() = dao.pickAny()
}

