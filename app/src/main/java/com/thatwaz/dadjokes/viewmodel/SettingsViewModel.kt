package com.thatwaz.dadjokes.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thatwaz.dadjokes.data.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    val useDynamicColor: StateFlow<Boolean> =
        SettingsDataStore.useDynamicColorFlow(context)
            .stateIn(viewModelScope, SharingStarted.Lazily, true)

    fun toggleDynamicColor() {
        viewModelScope.launch {
            val current = useDynamicColor.value
            SettingsDataStore.saveUseDynamicColor(context, !current)
        }
    }
}




