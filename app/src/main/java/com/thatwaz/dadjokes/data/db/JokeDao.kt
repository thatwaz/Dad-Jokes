package com.thatwaz.dadjokes.data.db



import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JokeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(joke: JokeEntity)

    @Query("SELECT * FROM jokes WHERE id = :id")
    suspend fun getJokeById(id: String): JokeEntity?

    @Query("SELECT * FROM jokes ORDER BY rating DESC")
    suspend fun getTopRated(): List<JokeEntity>


    @Query("SELECT * FROM jokes WHERE isFavorite = 1 ORDER BY id DESC")
    fun getFavoriteJokes(): Flow<List<JokeEntity>>

    @Query("SELECT * FROM jokes")
    suspend fun getAllJokesNow(): List<JokeEntity>


}
