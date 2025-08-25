package com.thatwaz.dadjokes.domain.model
import com.thatwaz.dadjokes.data.db.JokeEntity

data class Joke(
    val id: Int,
    val type: String,
    val setup: String,
    val punchline: String,
    val rating: Int = 0,
    val isFavorite: Boolean = false
)




fun Joke.toEntity(): JokeEntity = JokeEntity(
    id = id,
    type = type,
    setup = setup,
    punchline = punchline,
    rating = rating,
    isFavorite = isFavorite
)

fun JokeEntity.toJoke(): Joke = Joke(
    id = id,
    type = type,
    setup = setup,
    punchline = punchline,
    rating = rating,
    isFavorite = isFavorite
)



