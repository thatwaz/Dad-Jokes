package com.thatwaz.dadjokes.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thatwaz.dadjokes.domain.model.SavedJokeDelivery
import com.thatwaz.dadjokes.domain.repository.SavedJokeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedJokesViewModel @Inject constructor(
    private val repository: SavedJokeRepository
) : ViewModel() {

    private val _untoldJokes = MutableStateFlow<List<SavedJokeDelivery>>(emptyList())
    val untoldJokes: StateFlow<List<SavedJokeDelivery>> = _untoldJokes.asStateFlow()

    private val _toldJokes = MutableStateFlow<List<SavedJokeDelivery>>(emptyList())
    val toldJokes: StateFlow<List<SavedJokeDelivery>> = _toldJokes.asStateFlow()

    fun loadUntoldJokes() {
        viewModelScope.launch {
            repository.getUntoldJokes().collect {
                _untoldJokes.value = it
            }
        }
    }

    fun loadToldJokes() {
        viewModelScope.launch {
            repository.getToldJokes().collect {
                _toldJokes.value = it
            }
        }
    }

    fun saveJokeToPerson(setup: String, punchline: String, person: String) {
        viewModelScope.launch {
            val saved = SavedJokeDelivery(
                setup = setup,
                punchline = punchline,
                personName = person
            )
            repository.saveJoke(saved)
        }
    }

    fun markJokeAsTold(jokeId: Int, reaction: Int) {
        viewModelScope.launch {
            repository.markAsTold(jokeId, reaction)
            loadUntoldJokes()
            loadToldJokes()
        }
    }

    fun deleteSavedJoke(joke: SavedJokeDelivery) {
        viewModelScope.launch {
            repository.deleteJoke(joke)
            loadUntoldJokes()
            loadToldJokes()
        }
    }
}
