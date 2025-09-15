// data/api/ToJokeMapper.kt
package com.thatwaz.dadjokes.data.api

import com.thatwaz.dadjokes.domain.model.Joke

fun JokeApiItem.toJoke(): Joke {
    val idInt = id ?: 0
    return when (type) {
        "single" -> {
            val s = joke ?: ""
            Joke(
                id = idInt,
                type = "single",
                setup = s,
                punchline = "",
                rating = 0,
                isFavorite = false
            )
        }
        "twopart" -> {
            val s = setup.orEmpty()
            val p = punchline.orEmpty()   // mapped from "delivery" in DTO
            Joke(
                id = idInt,
                type = "twopart",
                setup = s,
                punchline = p,
                rating = 0,
                isFavorite = false
            )
        }
        else -> {
            // Defensive: treat unknown types as single-line text if present
            Joke(
                id = idInt,
                type = type ?: "single",
                setup = (joke ?: setup ?: "").orEmpty(),
                punchline = (punchline ?: "").orEmpty(),
                rating = 0,
                isFavorite = false
            )
        }
    }
}





