package com.thatwaz.dadjokes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thatwaz.dadjokes.data.repository.PrefsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: PrefsRepo
) : ViewModel() {
    val hasOnboarded: StateFlow<Boolean> =
        prefs.hasOnboarded.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun complete() = viewModelScope.launch { prefs.setOnboarded(true) }
}
