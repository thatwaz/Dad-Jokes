package com.thatwaz.dadjokes.data.db


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OwnedJokeDao {
    @Query("SELECT * FROM owned_jokes")
    suspend fun getAll(): List<OwnedJoke>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(joke: OwnedJoke)

    @Query("DELETE FROM owned_jokes WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM owned_jokes")
    suspend fun count(): Int
}

