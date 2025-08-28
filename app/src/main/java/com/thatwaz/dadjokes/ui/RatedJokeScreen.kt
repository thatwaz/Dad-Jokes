package com.thatwaz.dadjokes.ui


import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thatwaz.dadjokes.ui.components.JokeCard
import com.thatwaz.dadjokes.viewmodel.JokeViewModel

@Composable
fun RatedJokesScreen(
    viewModel: JokeViewModel = hiltViewModel()
) {
    val jokes by viewModel.ratedJokes.collectAsState(initial = emptyList())


    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(jokes) { joke ->
            JokeCard(
                joke = joke,
                ratingMessage = getRatingMessage(joke.rating),
                onToggleFavorite = { viewModel.toggleFavorite(joke) },
                onShare = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "${joke.setup} ${joke.punchline}")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share joke via:"))
                }
            )
        }
    }
}

fun getRatingMessage(rating: Int): String = when (rating) {
    1 -> "Oof. That one hurt 😒"
    2 -> "Meh."
    3 -> "Not bad!"
    4 -> "That got a chuckle 😆"
    5 -> "ROFL! 😂"
    else -> ""
}

