package com.thatwaz.dadjokes.data.api

import retrofit2.http.GET
import retrofit2.http.Headers

interface JokeApiService {
    @Headers("Accept: application/json")
    @GET("/")
    suspend fun getRandomJoke(): JokeDto
}

