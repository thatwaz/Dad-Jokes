package com.thatwaz.dadjokes.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CachedJokeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(rows: List<CachedJokeEntity>)

    @Query("""
        SELECT * FROM cached_jokes 
        WHERE hash NOT IN (
            SELECT id FROM seen_jokes 
            WHERE lastSeen >= :cutoff
        )
        ORDER BY RANDOM()
        LIMIT 1
    """)
    suspend fun pickUnseen(cutoff: Long): CachedJokeEntity?

    @Query("DELETE FROM cached_jokes WHERE fetchedAt < :cutoffOld")
    suspend fun dropOlderThan(cutoffOld: Long)

    @Query("SELECT COUNT(*) FROM cached_jokes")
    suspend fun count(): Int

    // DAO
    @Query("SELECT * FROM cached_jokes ORDER BY RANDOM() LIMIT 1")
    suspend fun pickAny(): CachedJokeEntity?



}

