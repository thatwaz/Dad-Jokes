package com.thatwaz.dadjokes.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// ---- DTOs ----

data class JokeApiFlags(
    val nsfw: Boolean = false,
    val religious: Boolean = false,
    val political: Boolean = false,
    val racist: Boolean = false,
    val sexist: Boolean = false,
    val explicit: Boolean = false,
)

data class JokeApiItem(
    val error: Boolean? = null,
    val category: String? = null,
    val type: String? = null,         // "single" | "twopart"
    val joke: String? = null,         // present if type == single
    val setup: String? = null,        // present if type == twopart
    @SerializedName("delivery")
    val punchline: String? = null,    // JokeAPI uses "delivery"
    val flags: JokeApiFlags? = null,
    val id: Int? = null,              // provider ID (for favorites)
    val safe: Boolean? = null,
    val lang: String? = null,
)

data class JokeApiBatch(
    val error: Boolean,
    val amount: Int? = null,
    val jokes: List<JokeApiItem>? = null
)

// ---- Retrofit interface ----

interface JokeApiService {

    /**
     * Batch fetch (amount ≤ 10).
     * Categories limited to clean-ish sets; adjust as needed.
     * `safe-mode` is enabled by passing any value (use "1").
     */
    @GET("joke/{categories}")
    suspend fun getJokes(
        @Path("categories") categories: String = "Misc,Pun",
        @Query("type") type: String = "single,twopart",
        @Query("amount") amount: Int = 10,
        @Query("lang") lang: String = "en",
        @Query("blacklistFlags")
        blacklistFlags: String = "nsfw,religious,political,racist,sexist,explicit",
        @Query("safe-mode") safeMode: String = "1"
    ): JokeApiBatch

    /**
     * Fetch a specific joke by provider ID (for favorites rehydration).
     * JokeAPI uses idRange for lookup.
     */

    // todo not used---------------------
    @GET("joke/Any")
    suspend fun getById(
        @Query("idRange") id: String,
        @Query("type") type: String = "single,twopart",
        @Query("lang") lang: String = "en",
        @Query("blacklistFlags")
        blacklistFlags: String = "nsfw,religious,political,racist,sexist,explicit",
        @Query("safe-mode") safeMode: String = "1"
    ): JokeApiItem
}

suspend fun JokeApiService.getRandomJokeSafe(): JokeApiItem {
    val batch = getJokes(amount = 1)
    return batch.jokes?.firstOrNull()
        ?: error("JokeAPI returned no jokes")
}


