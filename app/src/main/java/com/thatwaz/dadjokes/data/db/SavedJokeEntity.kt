package com.thatwaz.dadjokes.data.db


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_joke_deliveries")
data class SavedJokeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val setup: String,
    val punchline: String,
    val personName: String,
    val hasBeenTold: Boolean = false,
    val reactionRating: Int? = null,
    val dateSaved: Long = System.currentTimeMillis()
)
