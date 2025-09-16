package com.thatwaz.dadjokes.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thatwaz.dadjokes.data.db.OwnedJoke
import com.thatwaz.dadjokes.data.db.OwnedJokeDao
import com.thatwaz.dadjokes.data.repository.CachedJokesRepo
import com.thatwaz.dadjokes.data.repository.JokeRepository
import com.thatwaz.dadjokes.data.repository.PrefsRepo
import com.thatwaz.dadjokes.data.repository.SeenJokesRepo
import com.thatwaz.dadjokes.data.source.LocalJokesSource
import com.thatwaz.dadjokes.domain.model.Joke
import com.thatwaz.dadjokes.domain.model.SavedJokeDelivery
import com.thatwaz.dadjokes.domain.repository.SavedJokeRepository
import com.thatwaz.dadjokes.notification.DailyJokeReceiver
import com.thatwaz.dadjokes.ui.util.stableJokeId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Calendar
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject



@HiltViewModel
class JokeViewModel @Inject constructor(
    private val repository: JokeRepository,          // not used (kept to minimize DI churn)
    private val prefsRepo: PrefsRepo,
    private val savedRepo: SavedJokeRepository,
    private val seenRepo: SeenJokesRepo,
    private val cachedRepo: CachedJokesRepo,         // not used (kept to minimize DI churn)
    private val localSource: LocalJokesSource,       // bundled + owned (Room)
    private val ownedJokeDao: OwnedJokeDao           // for user submissions
) : ViewModel() {

    // ---- Single source of truth for the UI ----
    private val _joke = MutableStateFlow<Joke?>(null)
    val joke: StateFlow<Joke?> = _joke.asStateFlow()

    // Navigation state (history + index)
    private val jokeHistory = mutableListOf<Joke>()
    private var currentIndex = -1

    // Button enablement for the UI
    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private val _canGoForward = MutableStateFlow(false)
    val canGoForward: StateFlow<Boolean> = _canGoForward.asStateFlow()

    // Other streams
    val ratedJokes: Flow<List<Joke>> = repository.getRatedJokes()
    @RequiresApi(Build.VERSION_CODES.O)
    val notificationTime: Flow<LocalTime> = prefsRepo.notificationTimeFlow

    // Prevent overlapping fetches
    private val loading = AtomicBoolean(false)

    // Tunables
    private val ttlDays = 14
    private val ttlMillis = ttlDays * 24L * 60 * 60 * 1000
    private val keepCount = 2000           // can be larger now since we're local-only

    // Session-level recent IDs to avoid immediate repeats
    private val sessionSeen = LruStringSet(400)

    init {
        viewModelScope.launch {
            seenRepo.purgeOlderThan(ttlMillis)
            fetchLocalAndAppend() // first load
        }
    }

    /** Content-only hash for local jokes (owned + bundled). */
    private fun contentHash(setup: String, punch: String): String {
        val raw = "${setup.trim()}|${punch.trim()}".lowercase()
        return raw.hashCode().toString()
    }

    /** Next button: move forward in history if possible, otherwise fetch locally. */
    fun loadNext() {
        if (currentIndex < jokeHistory.lastIndex) {
            currentIndex++
            _joke.value = jokeHistory[currentIndex]
            updateNavFlags()
            return
        }
        if (!loading.compareAndSet(false, true)) return
        viewModelScope.launch {
            try {
                fetchLocalAndAppend()
            } finally {
                loading.set(false)
            }
        }
    }

    /** Kept for compatibility */
    fun showNextJoke() = loadNext()

    fun showPreviousJoke() {
        if (currentIndex > 0) {
            currentIndex--
            _joke.value = jokeHistory[currentIndex]
            updateNavFlags()
        }
    }

    fun rateCurrentJoke(rating: Int) {
        _joke.value = _joke.value?.copy(rating = rating)?.also { updated ->
            viewModelScope.launch { repository.saveRating(updated) }
            if (currentIndex in jokeHistory.indices) jokeHistory[currentIndex] = updated
        }
    }

    // -------- User submissions --------

    fun addUserSubmittedJoke(setup: String, punchline: String, credit: String? = null) {
        val s = setup.trim()
        val p = punchline.trim()
        if (s.isEmpty()) return
        // Use content-based ID so identical submissions upsert neatly
        val id = "user_" + contentHash(s, p)
        viewModelScope.launch {
            ownedJokeDao.upsert(OwnedJoke(id = id, setup = s, punchline = p /* add credit if you extend schema */))
        }
    }

    // -------- Save flows --------

    fun saveJokeToPeople(joke: Joke, people: List<String>) {
        if (people.isEmpty()) return
        viewModelScope.launch {
            people.forEach { name ->
                savedRepo.saveJoke(
                    SavedJokeDelivery(
                        setup = joke.setup,
                        punchline = joke.punchline,
                        personName = name
                    )
                )
            }
        }
    }

    val peopleNames: StateFlow<List<String>> =
        savedRepo.getPeople()
            .map { it.map { p -> p.personName }.sortedBy { it.lowercase() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveCurrentJokeToPerson(person: String) = saveCurrentJokeToPeople(listOf(person))

    fun saveCurrentJokeToPeople(people: List<String>) {
        val current = _joke.value ?: return
        if (people.isEmpty()) return
        viewModelScope.launch {
            people.forEach { name ->
                savedRepo.saveJoke(
                    SavedJokeDelivery(
                        setup = current.setup,
                        punchline = current.punchline,
                        personName = name
                    )
                )
            }
        }
    }

    fun deleteRatedJoke(joke: Joke) {
        viewModelScope.launch { repository.deleteRating(joke) }
    }

    fun clearAllRatedJokes() {
        viewModelScope.launch { repository.clearAllRatings() }
    }

    // -------- Notifications (unchanged) --------

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveNotificationTime(time: LocalTime) {
        viewModelScope.launch { prefsRepo.saveNotificationTime(time) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleDailyJokeNotification(context: Context, time: LocalTime) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyJokeReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
        }
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    // -------- Internals (LOCAL ONLY) --------

    /**
     * Local-only:
     * 1) Try local (owned + bundled) unseen & not in-session.
     * 2) If empty (tiny pool / all seen in 14d), relax rules progressively to avoid a dead end.
     */
    private suspend fun fetchLocalAndAppend() {
        // 1) strict: not seen in 14d AND not in current session
        localSource.pickLocalUnseen(
            wasSeenWithin = { hash -> seenRepo.wasSeenWithin(hash, ttlMillis) },
            sessionContains = { hash -> sessionSeen.contains(hash) }
        )?.let { local ->
            val h = contentHash(local.setup, local.punchline)
            seenRepo.markSeen(h, keepCount)
            sessionSeen.add(h)
            addToHistory(local)
            return
        }

        // 2) relaxed A: ignore 14d TTL but still avoid session duplicates
        localSource.pickLocalUnseen(
            wasSeenWithin = { _ -> false },                  // ignore TTL
            sessionContains = { hash -> sessionSeen.contains(hash) }
        )?.let { local ->
            val h = contentHash(local.setup, local.punchline)
            // do NOT re-mark seen; it was already marked; just add to session
            sessionSeen.add(h)
            addToHistory(local)
            return
        }

        // 3) relaxed B: allow session repeats as absolute last resort
        localSource.pickLocalUnseen(
            wasSeenWithin = { _ -> false },
            sessionContains = { _ -> false }                // allow any
        )?.let { local ->
            val h = contentHash(local.setup, local.punchline)
            sessionSeen.add(h)
            addToHistory(local)
            return
        }

        Log.w("JokeViewModel", "Local pool exhausted (even with relaxed rules). Add more jokes!")
    }

    /** Append a joke as the new tail of history, trimming any forward items, and update UI. */
    private fun addToHistory(newJoke: Joke) {
        if (currentIndex < jokeHistory.lastIndex) {
            jokeHistory.subList(currentIndex + 1, jokeHistory.size).clear()
        }
        val isSameAsLast = jokeHistory.lastOrNull()?.let {
            it.setup == newJoke.setup && it.punchline == newJoke.punchline
        } == true
        if (isSameAsLast) {
            currentIndex = jokeHistory.lastIndex
            _joke.value = jokeHistory[currentIndex]
            updateNavFlags()
            return
        }
        jokeHistory.add(newJoke)
        currentIndex = jokeHistory.lastIndex
        _joke.value = newJoke
        updateNavFlags()
    }

    private fun updateNavFlags() {
        _canGoBack.value = currentIndex > 0
        _canGoForward.value = currentIndex < jokeHistory.lastIndex
    }
}

/** Tiny LRU set to avoid immediate session repeats (uses java.util deque). */
private class LruStringSet(private val max: Int) {
    private val order = java.util.ArrayDeque<String>(max)
    private val set = HashSet<String>(max)

    fun add(v: String): Boolean {
        if (!set.add(v)) return false
        order.addLast(v)
        if (order.size > max) {
            val old = order.removeFirst()
            set.remove(old)
        }
        return true
    }

    fun contains(v: String) = set.contains(v)
}






//package com.thatwaz.dadjokes.viewmodel
//
//import android.app.AlarmManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.util.Log
//import androidx.annotation.RequiresApi
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.thatwaz.dadjokes.data.repository.CachedJokesRepo
//import com.thatwaz.dadjokes.data.repository.JokeRepository
//import com.thatwaz.dadjokes.data.repository.PrefsRepo
//import com.thatwaz.dadjokes.data.repository.SeenJokesRepo
//import com.thatwaz.dadjokes.domain.model.Joke
//import com.thatwaz.dadjokes.domain.model.JokeUiState
//import com.thatwaz.dadjokes.domain.model.SavedJokeDelivery
//import com.thatwaz.dadjokes.domain.repository.SavedJokeRepository
//import com.thatwaz.dadjokes.notification.DailyJokeReceiver
//import com.thatwaz.dadjokes.ui.util.stableJokeId
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.SharingStarted
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.map
//import kotlinx.coroutines.flow.stateIn
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import java.time.LocalTime
//import java.util.Calendar
//import java.util.concurrent.atomic.AtomicBoolean
//import javax.inject.Inject
//
//
//@HiltViewModel
//class JokeViewModel @Inject constructor(
//    private val repository: JokeRepository,
//    private val prefsRepo: PrefsRepo,
//    private val savedRepo: SavedJokeRepository,
//    private val seenRepo: SeenJokesRepo,
//    private val cachedRepo: CachedJokesRepo // 👈 NEW
//) : ViewModel() {
//
//    private val _joke = MutableStateFlow<Joke?>(null)
//    val joke: StateFlow<Joke?> = _joke.asStateFlow()
//
//    // Navigation state (history + index)
//    private val jokeHistory = mutableListOf<Joke>()
//    private var currentIndex = -1
//
//    // Button enablement as StateFlow for the UI
//    private val _canGoBack = MutableStateFlow(false)
//    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()
//
//    private val _canGoForward = MutableStateFlow(false)
//    val canGoForward: StateFlow<Boolean> = _canGoForward.asStateFlow()
//
//    // Keep your rated jokes list as-is
//    val ratedJokes: Flow<List<Joke>> = repository.getRatedJokes()
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    val notificationTime: Flow<LocalTime> = prefsRepo.notificationTimeFlow
//
//    private val loading = AtomicBoolean(false)
//
//
//    private val _state = MutableStateFlow(JokeUiState())
//    val state: StateFlow<JokeUiState> = _state.asStateFlow()
//
//    // Tunables (safe defaults)
//    private val ttlDays = 14
//    private val ttlMillis = ttlDays * 24L * 60 * 60 * 1000
//    private val keepCount = 500
//    private val maxAttempts = 6
//
//    init {
//
//
//        viewModelScope.launch {
//            // light housekeeping
//            seenRepo.purgeOlderThan(ttlMillis)
//            fetchJoke() // loads first joke and seeds history
//        }
//    }
//
//    fun loadNext() {
//        if (!loading.compareAndSet(false, true)) return  // ignore double-taps
//        viewModelScope.launch {
//            try {
//                val joke = repository.getJoke()
//                _state.update { it.copy(current = joke) }
//            } catch (t: Throwable) {
//                _state.update { it.copy(error = t.message ?: "Failed to load joke") }
//            } finally {
//                loading.set(false)
//            }
//        }
//    }
//
//    /** Public: Next/Previous API for the UI */
//
//    fun showPreviousJoke() {
//        if (currentIndex > 0) {
//            currentIndex--
//            _joke.value = jokeHistory[currentIndex]
//            updateNavFlags()
//        }
//    }
//
//    fun showNextJoke() {
//        // If there's a forward item in history, advance into it
//        if (currentIndex < jokeHistory.lastIndex) {
//            currentIndex++
//            _joke.value = jokeHistory[currentIndex]
//            updateNavFlags()
//        } else {
//            // Otherwise fetch a new one and append to history
//            fetchJoke()
//        }
//    }
//
//    /** Fetch a (new if possible) joke and push it onto history */
//    fun fetchJoke() {
//        viewModelScope.launch {
//            try {
//                var last: Joke? = null
//
//                repeat(maxAttempts) {
//                    val candidate = repository.getJoke()
//                    last = candidate
//
//                    val id = stableJokeId(
//                        candidate.setup,
//                        candidate.punchline,
//                        candidate.id.toString()
//                    )
//
//                    val recentlySeen = seenRepo.wasSeenWithin(id, ttlMillis)
//
//                    // Cache every candidate (helps offline later)
//                    cachedRepo.insert(
//                        setup = candidate.setup,
//                        punch = candidate.punchline,
//                        apiId = candidate.id,
//                        hash = id,
//                        type = candidate.type ?: "cached"
//                    )
//
//                    if (!recentlySeen) {
//                        seenRepo.markSeen(id, keepCount)
//                        addToHistory(candidate)
//                        return@launch
//                    }
//                }
//
//                // If all attempts were repeats, accept last (still cached above)
//                last?.let {
//                    val id = stableJokeId(it.setup, it.punchline, it.id.toString())
//                    seenRepo.markSeen(id, keepCount)
//                    addToHistory(it)
//                }
//            } catch (e: Exception) {
//                // Network failed — fallback to cache
//                val cutoff = System.currentTimeMillis() - ttlMillis
//                val cached = cachedRepo.pickUnseen(cutoff)
//                if (cached != null) {
//                    val joke = Joke(
//                        id = cached.apiId ?: 0,
//                        type = "cached",
//                        setup = cached.setup,
//                        punchline = cached.punchline,
//                        rating = 0
//                    )
//                    val id = cached.hash
//                    seenRepo.markSeen(id, keepCount)
//                    addToHistory(joke)
//                } else {
//                    Log.w("JokeViewModel", "No network and cache empty.")
//                }
//            }
//        }
//    }
//
//    /** Append a joke as the new tail of history, trimming any forward items */
//    private fun addToHistory(newJoke: Joke) {
//        // If we've navigated back in history, drop everything after the current index
//        if (currentIndex < jokeHistory.lastIndex) {
//            jokeHistory.subList(currentIndex + 1, jokeHistory.size).clear()
//        }
//
//        // Avoid accidental adjacent duplicates (optional)
//        if (jokeHistory.lastOrNull()?.let { it.setup == newJoke.setup && it.punchline == newJoke.punchline } == true) {
//            // Even if same, move index to end to keep nav flags correct
//            currentIndex = jokeHistory.lastIndex
//            _joke.value = jokeHistory[currentIndex]
//            updateNavFlags()
//            return
//        }
//
//        jokeHistory.add(newJoke)
//        currentIndex = jokeHistory.lastIndex
//        _joke.value = newJoke
//        updateNavFlags()
//    }
//
//    private fun updateNavFlags() {
//        _canGoBack.value = currentIndex > 0
//        _canGoForward.value = currentIndex < jokeHistory.lastIndex
//    }
//
//    /** Rating + Save flows (unchanged) */
//
//    fun rateCurrentJoke(rating: Int) {
//        _joke.value = _joke.value?.copy(rating = rating)?.also { updated ->
//            viewModelScope.launch { repository.saveRating(updated) }
//        }
//    }
//
//    fun saveJokeToPeople(joke: Joke, people: List<String>) {
//        if (people.isEmpty()) return
//        viewModelScope.launch {
//            people.forEach { name ->
//                savedRepo.saveJoke(
//                    SavedJokeDelivery(
//                        setup = joke.setup,
//                        punchline = joke.punchline,
//                        personName = name
//                    )
//                )
//            }
//        }
//    }
//
//    val peopleNames: StateFlow<List<String>> =
//        savedRepo.getPeople()
//            .map { it.map { p -> p.personName }.sortedBy { it.lowercase() } }
//            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
//
//    fun saveCurrentJokeToPerson(person: String) = saveCurrentJokeToPeople(listOf(person))
//
//    fun saveCurrentJokeToPeople(people: List<String>) {
//        val current = _joke.value ?: return
//        if (people.isEmpty()) return
//        viewModelScope.launch {
//            people.forEach { name ->
//                savedRepo.saveJoke(
//                    SavedJokeDelivery(
//                        setup = current.setup,
//                        punchline = current.punchline,
//                        personName = name
//                    )
//                )
//            }
//        }
//    }
//
//    fun deleteRatedJoke(joke: Joke) {
//        viewModelScope.launch {
//            repository.deleteRating(joke)
//        }
//    }
//
//    fun clearAllRatedJokes() {
//        viewModelScope.launch {
//            repository.clearAllRatings()
//        }
//    }
//
//    // Notifications (unchanged)
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun saveNotificationTime(time: LocalTime) {
//        viewModelScope.launch { prefsRepo.saveNotificationTime(time) }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun scheduleDailyJokeNotification(context: Context, time: LocalTime) {
//        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val intent = Intent(context, DailyJokeReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(
//            context, 0, intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//        val calendar = Calendar.getInstance().apply {
//            timeInMillis = System.currentTimeMillis()
//            set(Calendar.HOUR_OF_DAY, time.hour)
//            set(Calendar.MINUTE, time.minute)
//            set(Calendar.SECOND, 0)
//            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
//        }
//        alarmManager.setRepeating(
//            AlarmManager.RTC_WAKEUP,
//            calendar.timeInMillis,
//            AlarmManager.INTERVAL_DAY,
//            pendingIntent
//        )
//    }
//}



//@HiltViewModel
//class JokeViewModel @Inject constructor(
//    private val repository: JokeRepository,
//    private val prefsRepo: PrefsRepo,
//    private val savedRepo: SavedJokeRepository
//) : ViewModel() {
//
//    private val _joke = MutableStateFlow<Joke?>(null)
//    val joke: StateFlow<Joke?> = _joke.asStateFlow()
//
//    private val jokeHistory = mutableListOf<Joke>()
//    private var currentIndex = -1
//
//    // Keep your rated jokes list as-is
//    val ratedJokes: Flow<List<Joke>> = repository.getRatedJokes()
//
//    // ⛔️ REMOVED: favoriteJokes, toggleFavorite(...), isCurrentJokeFavorited
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    val notificationTime: Flow<LocalTime> = prefsRepo.notificationTimeFlow
//
//    init {
//        fetchJoke()
//    }
//
//    fun fetchJoke() {
//        viewModelScope.launch {
//            try {
//                val newJoke = repository.getJoke()
//                if (currentIndex < jokeHistory.lastIndex) {
//                    jokeHistory.subList(currentIndex + 1, jokeHistory.size).clear()
//                }
//                jokeHistory.add(newJoke)
//                currentIndex++
//                _joke.value = newJoke
//            } catch (e: Exception) {
//                Log.e("JokeViewModel", "Error fetching joke: ${e.localizedMessage}")
//            }
//        }
//    }
//
//    fun showPreviousJoke() {
//        if (canGoBack()) {
//            currentIndex--
//            _joke.value = jokeHistory[currentIndex]
//        }
//    }
//
//    fun canGoBack(): Boolean = currentIndex > 0
//
//    fun rateCurrentJoke(rating: Int) {
//        _joke.value = _joke.value?.copy(rating = rating)?.also { updated ->
//            viewModelScope.launch { repository.saveRating(updated) }
//        }
//    }
//
//    // In JokeViewModel
//    fun saveJokeToPeople(joke: Joke, people: List<String>) {
//        if (people.isEmpty()) return
//        viewModelScope.launch {
//            people.forEach { name ->
//                savedRepo.saveJoke(
//                    SavedJokeDelivery(
//                        setup = joke.setup,
//                        punchline = joke.punchline,
//                        personName = name
//                    )
//                )
//            }
//        }
//    }
//
//    // Existing imports likely include kotlinx.coroutines.flow.*
//    val peopleNames: StateFlow<List<String>> =
//        savedRepo.getPeople() // Flow<List<PersonSummary>>
//            .map { it.map { p -> p.personName }.sortedBy { it.lowercase() } }
//            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
//
//
//    /* ===========================
//       NEW: Save-to-people helpers
//       =========================== */
//
//    /** Save the current joke for a single person */
//    fun saveCurrentJokeToPerson(person: String) {
//        saveCurrentJokeToPeople(listOf(person))
//    }
//
//    /** Save the current joke for multiple people at once */
//    fun saveCurrentJokeToPeople(people: List<String>) {
//        val current = _joke.value ?: return
//        if (people.isEmpty()) return
//        viewModelScope.launch {
//            // If you implemented saveJokeToPeople in the repo, use it:
//            // savedRepo.saveJokeToPeople(current.setup, current.punchline, people)
//
//            // Or fallback to per-person inserts using your existing save method:
//            people.forEach { name ->
//                savedRepo.saveJoke(
//                    SavedJokeDelivery(
//                        setup = current.setup,
//                        punchline = current.punchline,
//                        personName = name
//                    )
//                )
//            }
//        }
//    }
//
//    /* ===========================
//       Notifications (unchanged)
//       =========================== */
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun saveNotificationTime(time: LocalTime) {
//        viewModelScope.launch {
//            prefsRepo.saveNotificationTime(time)
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun scheduleDailyJokeNotification(context: Context, time: LocalTime) {
//        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//
//        val intent = Intent(context, DailyJokeReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(
//            context,
//            0,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val calendar = Calendar.getInstance().apply {
//            timeInMillis = System.currentTimeMillis()
//            set(Calendar.HOUR_OF_DAY, time.hour)
//            set(Calendar.MINUTE, time.minute)
//            set(Calendar.SECOND, 0)
//            if (before(Calendar.getInstance())) {
//                add(Calendar.DAY_OF_MONTH, 1)
//            }
//        }
//
//        alarmManager.setRepeating(
//            AlarmManager.RTC_WAKEUP,
//            calendar.timeInMillis,
//            AlarmManager.INTERVAL_DAY,
//            pendingIntent
//        )
//    }
//}


//@HiltViewModel
//class JokeViewModel @Inject constructor(
//    private val repository: JokeRepository,
//    private val prefsRepo: PrefsRepo,
//    private val savedRepo: SavedJokeRepository
//) : ViewModel() {
//
//    private val _joke = MutableStateFlow<Joke?>(null)
//    val joke: StateFlow<Joke?> = _joke.asStateFlow()
//
//    private val jokeHistory = mutableListOf<Joke>()
//    private var currentIndex = -1
//
//    val ratedJokes: Flow<List<Joke>> = repository.getRatedJokes()
//
//
//
//    val favoriteJokes: StateFlow<List<Joke>> = repository
//        .getFavorites()
//        .map { jokes ->
//            jokes.sortedWith(
//                compareByDescending<Joke> { it.rating }
//                    .thenBy { it.setup + " " + it.punchline }
//            )
//        }
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    val notificationTime: Flow<LocalTime> = prefsRepo.notificationTimeFlow
//
//
//    init {
//        fetchJoke()
//    }
//
//    fun fetchJoke() {
//        viewModelScope.launch {
//            try {
//                val newJoke = repository.getJoke()
//                if (currentIndex < jokeHistory.lastIndex) {
//                    jokeHistory.subList(currentIndex + 1, jokeHistory.size).clear()
//                }
//                jokeHistory.add(newJoke)
//                currentIndex++
//                _joke.value = newJoke
//            } catch (e: Exception) {
//                Log.e("JokeViewModel", "Error fetching joke: ${e.localizedMessage}")
//            }
//        }
//    }
//
//    fun showPreviousJoke() {
//        if (canGoBack()) {
//            currentIndex--
//            _joke.value = jokeHistory[currentIndex]
//        }
//    }
//
//    fun canGoBack(): Boolean = currentIndex > 0
//
//    fun rateCurrentJoke(rating: Int) {
//        _joke.value = _joke.value?.copy(rating = rating)?.also { updated ->
//            viewModelScope.launch { repository.saveRating(updated) }
//        }
//    }
//
//    // 1. For single joke detail (uses internal _joke state)
//    fun toggleFavorite() {
//        _joke.value = _joke.value?.copy(isFavorite = !_joke.value!!.isFavorite)?.also { updated ->
//            viewModelScope.launch { repository.saveRating(updated) }
//        }
//    }
//
//    // 2. For lists of jokes like favorites or ratings (passes in specific joke)
//    fun toggleFavorite(joke: Joke) {
//        viewModelScope.launch {
//            val updated = joke.copy(isFavorite = !joke.isFavorite)
//            repository.saveRating(updated)
//        }
//    }
//
//
//    val isCurrentJokeFavorited: StateFlow<Boolean> = combine(joke, favoriteJokes) { currentJoke, favorites ->
//        currentJoke?.let { joke -> favorites.any { it.id == joke.id } } ?: false
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun saveNotificationTime(time: LocalTime) {
//        viewModelScope.launch {
//            prefsRepo.saveNotificationTime(time)
//        }
//    }
//
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun scheduleDailyJokeNotification(context: Context, time: LocalTime) {
//        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//
//        val intent = Intent(context, DailyJokeReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(
//            context,
//            0,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val calendar = Calendar.getInstance().apply {
//            timeInMillis = System.currentTimeMillis()
//            set(Calendar.HOUR_OF_DAY, time.hour)
//            set(Calendar.MINUTE, time.minute)
//            set(Calendar.SECOND, 0)
//            if (before(Calendar.getInstance())) {
//                add(Calendar.DAY_OF_MONTH, 1) // Schedule for next day if time has passed
//            }
//        }
//
//        alarmManager.setRepeating(
//            AlarmManager.RTC_WAKEUP,
//            calendar.timeInMillis,
//            AlarmManager.INTERVAL_DAY,
//            pendingIntent
//        )
//    }
//
//}
//
//
