package com.thatwaz.dadjokes.data.api

import com.google.gson.annotations.SerializedName
import com.thatwaz.dadjokes.domain.model.Joke

data class JokeDto(
    val id: String,
    val joke: String,
    @SerializedName("status") val status: Int
) {
    fun toJoke(): Joke = Joke(id = id, joke = joke)
}

