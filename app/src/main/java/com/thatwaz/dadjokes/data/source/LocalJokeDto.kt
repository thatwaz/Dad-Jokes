package com.thatwaz.dadjokes.data.source

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thatwaz.dadjokes.R
import com.thatwaz.dadjokes.data.db.OwnedJokeDao
import com.thatwaz.dadjokes.domain.model.Joke
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class LocalJokeDto(val id: String, val setup: String, val punchline: String)

/** Content-only hash so local jokes are marked/checked consistently with the VM. */
private fun contentHash(setup: String, punch: String): String {
    val raw = "${setup.trim()}|${punch.trim()}".lowercase()
    return raw.hashCode().toString()
}

@Singleton
class LocalJokesSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ownedDao: OwnedJokeDao
) {
    private val gson = Gson()

    // Load bundled jokes once
    private val bundled: List<LocalJokeDto> by lazy {
        runCatching {
            context.resources.openRawResource(R.raw.jokes_seed)
                .bufferedReader().use { it.readText() }
        }.mapCatching { json ->
            val type = object : TypeToken<List<LocalJokeDto>>() {}.type
            gson.fromJson<List<LocalJokeDto>>(json, type)
        }.getOrElse { emptyList() }
    }

    /** Returns a local (owned or bundled) joke that isn't recently-seen or in-session, else null. */
    suspend fun pickLocalUnseen(
        wasSeenWithin: suspend (hash: String) -> Boolean,
        sessionContains: (hash: String) -> Boolean
    ): Joke? {
        val owned: List<LocalJokeDto> = runCatching {
            ownedDao.getAll().map { LocalJokeDto(it.id, it.setup, it.punchline) }
        }.getOrDefault(emptyList())

        // Merge and shuffle for variety
        val pool = (owned + bundled).shuffled()

        for (r in pool) {
            val hash = contentHash(r.setup, r.punchline)  // 👈 content-only hash
            if (!sessionContains(hash) && !wasSeenWithin(hash)) {
                return Joke(
                    id = r.id.hashCode(),   // domain id (local)
                    type = "local",
                    setup = r.setup,
                    punchline = r.punchline,
                    rating = 0
                )
            }
        }
        return null
    }
}


