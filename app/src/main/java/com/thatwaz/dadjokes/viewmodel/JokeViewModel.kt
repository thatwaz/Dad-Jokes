package com.thatwaz.dadjokes.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thatwaz.dadjokes.data.repository.JokeRepository
import com.thatwaz.dadjokes.domain.model.Joke
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JokeViewModel @Inject constructor(
    private val repository: JokeRepository
) : ViewModel() {

    private val _joke = MutableStateFlow<Joke?>(null)
    val joke: StateFlow<Joke?> = _joke.asStateFlow()

    private val jokeHistory = mutableListOf<Joke>()
    private var currentIndex = -1

    val favoriteJokes: StateFlow<List<Joke>> = repository
        .getFavorites()
        .map { jokes -> jokes.sortedWith(compareByDescending<Joke> { it.rating }.thenBy { it.joke }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun toggleFavorite() {
        _joke.value = _joke.value?.copy(isFavorite = !_joke.value!!.isFavorite)?.also { updated ->
            viewModelScope.launch { repository.saveRating(updated) }
        }
    }

    val isCurrentJokeFavorited: StateFlow<Boolean> = combine(joke, favoriteJokes) { currentJoke, favorites ->
        currentJoke?.let { joke -> favorites.any { it.id == joke.id } } ?: false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
}


