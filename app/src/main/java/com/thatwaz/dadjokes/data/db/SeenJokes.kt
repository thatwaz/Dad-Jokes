package com.thatwaz.dadjokes.data.db


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seen_jokes")
data class SeenJoke(
    @PrimaryKey val id: String,
    val lastSeen: Long = System.currentTimeMillis()
)

