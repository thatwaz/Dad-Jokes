package com.thatwaz.dadjokes.data.api

import com.thatwaz.dadjokes.domain.model.Joke

data class JokeDto(
    val id: String,
    val joke: String,
    val status: Int
)


fun JokeDto.toJoke(): Joke {
    val questionMarkIndex = joke.indexOf('?')

    return if (questionMarkIndex != -1 && questionMarkIndex < joke.length - 1) {
        Joke(
            id = id.hashCode(),
            type = "twopart",
            setup = joke.substring(0, questionMarkIndex + 1).trim(),
            punchline = joke.substring(questionMarkIndex + 1).trim(),
            rating = 0,
            isFavorite = false
        )
    } else {
        Joke(
            id = id.hashCode(),
            type = "single",
            setup = joke,
            punchline = "",
            rating = 0,
            isFavorite = false
        )
    }
}




