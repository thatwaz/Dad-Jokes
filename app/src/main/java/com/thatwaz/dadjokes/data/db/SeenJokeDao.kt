package com.thatwaz.dadjokes.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SeenJokeDao {

    @Query("SELECT EXISTS(SELECT 1 FROM seen_jokes WHERE id = :id AND lastSeen >= :cutoff)")
    suspend fun existsWithin(id: String, cutoff: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: SeenJoke)

    // Keep newest `keep` rows, drop the rest
    @Query("""
        DELETE FROM seen_jokes 
        WHERE id IN (
            SELECT id FROM seen_jokes
            ORDER BY lastSeen DESC
            LIMIT -1 OFFSET :keep
        )
    """)
    suspend fun trimTo(keep: Int)

    // Optional: proactively purge items older than TTL
    @Query("DELETE FROM seen_jokes WHERE lastSeen < :cutoff")
    suspend fun dropOlderThan(cutoff: Long)
}
