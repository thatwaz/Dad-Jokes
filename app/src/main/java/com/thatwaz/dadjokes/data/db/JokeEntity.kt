package com.thatwaz.dadjokes.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jokes")
data class JokeEntity(
    @PrimaryKey val id: String,
    val joke: String,
    val rating: Int = 0,
    val isFavorite: Boolean = false
)
