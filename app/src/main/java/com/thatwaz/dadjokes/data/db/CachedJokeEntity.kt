package com.thatwaz.dadjokes.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cached_jokes",
    indices = [Index(value = ["hash"], unique = true)]
)
data class CachedJokeEntity(
    @PrimaryKey(autoGenerate = true) val pk: Long = 0L,
    val apiId: Int?,
    val type: String?,          // include if your Joke has this; else remove in both places
    val setup: String,
    val punchline: String,
    val hash: String,
    val fetchedAt: Long = System.currentTimeMillis()
)


