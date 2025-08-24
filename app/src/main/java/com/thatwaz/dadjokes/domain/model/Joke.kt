package com.thatwaz.dadjokes.domain.model
import com.thatwaz.dadjokes.data.db.JokeEntity

data class Joke(
    val id: String,
    val joke: String,
    val rating: Int = 0,
    val isFavorite: Boolean = false
)



fun Joke.toEntity(): JokeEntity = JokeEntity(
    id = id,
    joke = joke,
    rating = rating,
    isFavorite = isFavorite
)

fun JokeEntity.toJoke(): Joke = Joke(
    id = id,
    joke = joke,
    rating = rating,
    isFavorite = isFavorite
)


