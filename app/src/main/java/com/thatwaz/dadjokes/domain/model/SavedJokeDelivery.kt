package com.thatwaz.dadjokes.domain.model


data class SavedJokeDelivery(
    val id: Int = 0,
    val setup: String,
    val punchline: String,
    val personName: String,
    val hasBeenTold: Boolean = false,
    val reactionRating: Int? = null, // e.g. 1–5 or emoji ID
    val dateSaved: Long = System.currentTimeMillis()
)

