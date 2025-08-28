package com.thatwaz.dadjokes.ui.home

import android.content.Intent
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thatwaz.dadjokes.ui.components.EmojiRatingBar
import com.thatwaz.dadjokes.ui.components.TypewriterText
import com.thatwaz.dadjokes.viewmodel.JokeViewModel

@RequiresApi(35)
@Composable
fun HomeScreen(viewModel: JokeViewModel = hiltViewModel()) {
    val jokeState by viewModel.joke.collectAsState()
    val isFavorited by viewModel.isCurrentJokeFavorited.collectAsState()

    val context = LocalContext.current
    var isPunchlineRevealed by remember { mutableStateOf(false) }
    var typingDone by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        jokeState?.let { joke ->
            val displaySetup: String
            val displayPunchline: String

            if (joke.setup.contains("?")) {
                displaySetup = joke.setup
                displayPunchline = joke.punchline
            } else if (joke.setup.contains(".")) {
                displaySetup = joke.setup.substringBefore(".").trim() + "."
                displayPunchline = joke.setup.substringAfter(".").trim()
            } else {
                displaySetup = joke.setup
                displayPunchline = joke.punchline
            }

            Column(
                modifier = Modifier
                    .clickable(enabled = typingDone && displayPunchline.isNotBlank()) {
                        isPunchlineRevealed = true
                    }
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TypewriterText(
                    fullText = displaySetup,
                    startDelayMillis = 500L, // Adjust this as you like
                    onTypingComplete = { typingDone = true }
                )


                if (isPunchlineRevealed && displayPunchline.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = displayPunchline,
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                } else if (displayPunchline.isNotBlank() && typingDone) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap to reveal punchline",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Text(
                text = when (joke.rating) {
                    1 -> "Oof. That one hurt 😒"
                    2 -> "Meh."
                    3 -> "Not bad!"
                    4 -> "That got a chuckle 😆"
                    5 -> "ROFL! 😂"
                    else -> "Rate this joke:"
                },
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            EmojiRatingBar(
                selectedRating = joke.rating,
                onRatingSelected = { viewModel.rateCurrentJoke(it) }
            )

            IconButton(
                onClick = {
                    if (isFavorited) {
                        Toast.makeText(context, "Already in favorites!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.toggleFavorite()
                    }
                },
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Icon(
                    imageVector = if (joke.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (joke.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (joke.isFavorite) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }

            IconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "${joke.setup} ${if (joke.punchline.isNotBlank()) joke.punchline else ""}"
                        )
                    }
                    context.startActivity(Intent.createChooser(intent, "Share this joke via:"))
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Joke",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                enabled = viewModel.canGoBack(),
                onClick = {
                    isPunchlineRevealed = false
                    typingDone = false
                    viewModel.showPreviousJoke()
                }
            ) {
                Text("Previous")
            }

            Button(
                onClick = {
                    isPunchlineRevealed = false
                    typingDone = false
                    viewModel.fetchJoke()
                }
            ) {
                Text("Next Joke")
            }
        }
    }
}











