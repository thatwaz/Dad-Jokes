package com.thatwaz.dadjokes.data.db


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "owned_jokes")
data class OwnedJoke(
    @PrimaryKey val id: String,      // e.g. "user_<hash>"
    val setup: String,
    val punchline: String,
    val createdAt: Long = System.currentTimeMillis()
)

