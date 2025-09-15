package com.thatwaz.dadjokes.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        JokeEntity::class,
        SavedJokeEntity::class,
        SeenJoke::class,
        CachedJokeEntity::class     // 👈 add
    ],
    version = 6,                    // 👈 bump (you used fallbackToDestructiveMigration)
    exportSchema = false
)
abstract class JokeDatabase : RoomDatabase() {
    abstract fun jokeDao(): JokeDao
    abstract fun savedJokeDao(): SavedJokeDao
    abstract fun seenJokeDao(): SeenJokeDao
    abstract fun cachedJokeDao(): CachedJokeDao   // 👈 add
}



