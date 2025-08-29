package com.thatwaz.dadjokes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thatwaz.dadjokes.domain.model.PersonSummary
import com.thatwaz.dadjokes.domain.repository.SavedJokeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// com.thatwaz.dadjokes.presentation.savedjokes.PeopleViewModel.kt
@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val repo: SavedJokeRepository
) : ViewModel() {
    val people: StateFlow<List<PersonSummary>> =
        repo.getPeople().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // PeopleViewModel.kt
    fun deletePerson(name: String) = viewModelScope.launch { repo.deleteAllForPerson(name) }
    fun deleteAll() = viewModelScope.launch { repo.deleteAllSaved() }

}
