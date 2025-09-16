package com.thatwaz.dadjokes.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        JokeEntity::class,
        SavedJokeEntity::class,
        SeenJoke::class,
        CachedJokeEntity::class,
        OwnedJoke::class                 // 👈 added
    ],
    version = 7,                         // 👈 bump (schema changed)
    exportSchema = false
)
abstract class JokeDatabase : RoomDatabase() {
    abstract fun jokeDao(): JokeDao
    abstract fun savedJokeDao(): SavedJokeDao
    abstract fun seenJokeDao(): SeenJokeDao
    abstract fun cachedJokeDao(): CachedJokeDao
    abstract fun ownedJokeDao(): OwnedJokeDao      // 👈 added
}




