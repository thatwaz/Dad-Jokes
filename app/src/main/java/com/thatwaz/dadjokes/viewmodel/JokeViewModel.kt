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
import com.thatwaz.dadjokes.data.repository.JokeRepository
import com.thatwaz.dadjokes.data.repository.PrefsRepo
import com.thatwaz.dadjokes.domain.model.Joke
import com.thatwaz.dadjokes.domain.model.SavedJokeDelivery
import com.thatwaz.dadjokes.domain.repository.SavedJokeRepository
import com.thatwaz.dadjokes.notification.DailyJokeReceiver
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
import javax.inject.Inject

@HiltViewModel
class JokeViewModel @Inject constructor(
    private val repository: JokeRepository,
    private val prefsRepo: PrefsRepo,
    private val savedRepo: SavedJokeRepository
) : ViewModel() {

    private val _joke = MutableStateFlow<Joke?>(null)
    val joke: StateFlow<Joke?> = _joke.asStateFlow()

    private val jokeHistory = mutableListOf<Joke>()
    private var currentIndex = -1

    // Keep your rated jokes list as-is
    val ratedJokes: Flow<List<Joke>> = repository.getRatedJokes()

    // ⛔️ REMOVED: favoriteJokes, toggleFavorite(...), isCurrentJokeFavorited

    @RequiresApi(Build.VERSION_CODES.O)
    val notificationTime: Flow<LocalTime> = prefsRepo.notificationTimeFlow

    init {
        fetchJoke()
    }

    fun fetchJoke() {
        viewModelScope.launch {
            try {
                val newJoke = repository.getJoke()
                if (currentIndex < jokeHistory.lastIndex) {
                    jokeHistory.subList(currentIndex + 1, jokeHistory.size).clear()
                }
                jokeHistory.add(newJoke)
                currentIndex++
                _joke.value = newJoke
            } catch (e: Exception) {
                Log.e("JokeViewModel", "Error fetching joke: ${e.localizedMessage}")
            }
        }
    }

    fun showPreviousJoke() {
        if (canGoBack()) {
            currentIndex--
            _joke.value = jokeHistory[currentIndex]
        }
    }

    fun canGoBack(): Boolean = currentIndex > 0

    fun rateCurrentJoke(rating: Int) {
        _joke.value = _joke.value?.copy(rating = rating)?.also { updated ->
            viewModelScope.launch { repository.saveRating(updated) }
        }
    }

    // In JokeViewModel
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

    // Existing imports likely include kotlinx.coroutines.flow.*
    val peopleNames: StateFlow<List<String>> =
        savedRepo.getPeople() // Flow<List<PersonSummary>>
            .map { it.map { p -> p.personName }.sortedBy { it.lowercase() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    /* ===========================
       NEW: Save-to-people helpers
       =========================== */

    /** Save the current joke for a single person */
    fun saveCurrentJokeToPerson(person: String) {
        saveCurrentJokeToPeople(listOf(person))
    }

    /** Save the current joke for multiple people at once */
    fun saveCurrentJokeToPeople(people: List<String>) {
        val current = _joke.value ?: return
        if (people.isEmpty()) return
        viewModelScope.launch {
            // If you implemented saveJokeToPeople in the repo, use it:
            // savedRepo.saveJokeToPeople(current.setup, current.punchline, people)

            // Or fallback to per-person inserts using your existing save method:
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

    /* ===========================
       Notifications (unchanged)
       =========================== */

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveNotificationTime(time: LocalTime) {
        viewModelScope.launch {
            prefsRepo.saveNotificationTime(time)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleDailyJokeNotification(context: Context, time: LocalTime) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, DailyJokeReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}


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
