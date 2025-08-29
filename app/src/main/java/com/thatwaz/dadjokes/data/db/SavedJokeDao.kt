package com.thatwaz.dadjokes.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedJokeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedJoke(joke: SavedJokeEntity)

    @Update
    suspend fun updateSavedJoke(joke: SavedJokeEntity)

    @Delete
    suspend fun deleteSavedJoke(joke: SavedJokeEntity)

    // SavedJokeDao.kt
    @Query("DELETE FROM saved_joke_deliveries WHERE personName = :name")
    suspend fun deleteAllForPerson(name: String)

    @Query("DELETE FROM saved_joke_deliveries")
    suspend fun deleteAllSaved()


    @Query("SELECT * FROM saved_joke_deliveries WHERE hasBeenTold = 0 ORDER BY dateSaved DESC")
    fun getUntoldJokes(): Flow<List<SavedJokeEntity>>

    @Query("SELECT * FROM saved_joke_deliveries WHERE hasBeenTold = 1 ORDER BY dateSaved DESC")
    fun getToldJokes(): Flow<List<SavedJokeEntity>>

    @Query("SELECT * FROM saved_joke_deliveries WHERE personName = :name ORDER BY dateSaved DESC")
    fun getJokesForPerson(name: String): Flow<List<SavedJokeEntity>>


    @Query("UPDATE saved_joke_deliveries SET hasBeenTold = 1, reactionRating = :reaction WHERE id = :jokeId")
    suspend fun markJokeAsTold(jokeId: Int, reaction: Int)

    @Query("UPDATE saved_joke_deliveries SET hasBeenTold = 0, reactionRating = NULL WHERE id = :id")
    suspend fun markUntold(id: Int)


    @Query("""
SELECT 
  personName AS personName,
  SUM(CASE WHEN hasBeenTold = 0 THEN 1 ELSE 0 END) AS untoldCount,
  SUM(CASE WHEN hasBeenTold = 1 THEN 1 ELSE 0 END) AS toldCount
FROM saved_joke_deliveries
GROUP BY personName
ORDER BY LOWER(personName)
""")
    fun getPeopleSummary(): Flow<List<PersonSummaryDb>>

    @Query("""
SELECT * FROM saved_joke_deliveries
WHERE personName = :name AND hasBeenTold = 0
ORDER BY dateSaved DESC
""")
    fun getUntoldForPerson(name: String): Flow<List<SavedJokeEntity>>

    @Query("""
SELECT * FROM saved_joke_deliveries
WHERE personName = :name AND hasBeenTold = 1
ORDER BY dateSaved DESC
""")
    fun getToldForPerson(name: String): Flow<List<SavedJokeEntity>>

}
