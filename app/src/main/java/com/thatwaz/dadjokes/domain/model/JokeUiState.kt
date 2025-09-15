package com.thatwaz.dadjokes.domain.model

// domain/model or ui package
data class JokeUiState(
    val current: Joke? = null,
    val error: String? = null,
    val isLoading: Boolean = false
)

