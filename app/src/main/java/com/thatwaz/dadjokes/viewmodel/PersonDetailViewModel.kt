package com.thatwaz.dadjokes.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thatwaz.dadjokes.domain.model.SavedJokeDelivery
import com.thatwaz.dadjokes.domain.repository.SavedJokeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// com.thatwaz.dadjokes.presentation.savedjokes.PersonDetailViewModel.kt
@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    private val repo: SavedJokeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val personName: String = checkNotNull(savedStateHandle["person"])

    val untold = repo.getUntoldForPerson(personName)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val told = repo.getToldForPerson(personName)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun markTold(id: Int, rating: Int) = viewModelScope.launch {
        repo.markAsTold(id, rating)
    }

    fun delete(item: SavedJokeDelivery) = viewModelScope.launch {
        repo.deleteJoke(item)
    }

    fun deleteAllForPerson() = viewModelScope.launch {
        repo.deleteAllForPerson(personName)
    }

    fun undoTold(id: Int) = viewModelScope.launch {
        repo.markAsUntold(id)
    }

}

