package com.thatwaz.dadjokes.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [JokeEntity::class, SavedJokeEntity::class], // Add SavedJokeEntity here
    version = 3
)
abstract class JokeDatabase : RoomDatabase() {
    abstract fun jokeDao(): JokeDao
    abstract fun savedJokeDao(): SavedJokeDao // <-- Add this
}
