package com.thatwaz.dadjokes.ui


import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thatwaz.dadjokes.domain.model.Joke
import com.thatwaz.dadjokes.ui.components.JokeCard
import com.thatwaz.dadjokes.ui.dialogs.SaveToPeopleDialog
import com.thatwaz.dadjokes.viewmodel.JokeViewModel

@Composable
fun RatedJokesScreen(
    viewModel: JokeViewModel = hiltViewModel()
) {
    val jokes by viewModel.ratedJokes.collectAsState(initial = emptyList())
    val existingPeople by viewModel.peopleNames.collectAsState() // ← add this
    val context = LocalContext.current

    var saveTarget by remember { mutableStateOf<Joke?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(jokes) { joke ->
            JokeCard(
                joke = joke,
                ratingMessage = getRatingMessage(joke.rating),
                onToggleFavorite = { saveTarget = joke }, // now opens save dialog
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

    if (saveTarget != null) {
        SaveToPeopleDialog(
            existingPeople = existingPeople,      // ← pass it here
            onDismiss = { saveTarget = null },
            onSave = { people ->
                saveTarget?.let { j ->
                    viewModel.saveJokeToPeople(j, people)
                    Toast.makeText(context, "Saved for ${people.joinToString()}", Toast.LENGTH_SHORT).show()
                }
                saveTarget = null
            }
        )
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

