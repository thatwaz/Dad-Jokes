package com.thatwaz.dadjokes.ui


import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thatwaz.dadjokes.domain.model.Joke
import com.thatwaz.dadjokes.ui.dialogs.SaveToPeopleDialog
import com.thatwaz.dadjokes.viewmodel.JokeViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RatedJokesScreen(
    viewModel: JokeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val jokes by viewModel.ratedJokes.collectAsState(initial = emptyList())
    val existingPeople by viewModel.peopleNames.collectAsState()

    // Dialog state
    var saveTarget by remember { mutableStateOf<Joke?>(null) }
    var deleteTarget by remember { mutableStateOf<Joke?>(null) }
    var confirmDeleteAll by remember { mutableStateOf(false) }

    // Organize by rating (5→1), ignore unrated
    val grouped = remember(jokes) {
        jokes
            .filter { it.rating in 1..5 }
            .groupBy { it.rating }
            .toSortedMap(compareByDescending { it }) // 5 → 1
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (jokes.isEmpty()) "Rated Jokes"
                        else "Rated Jokes (${jokes.size})"
                    )
                },
                actions = {
                    IconButton(
                        enabled = jokes.isNotEmpty(),
                        onClick = { confirmDeleteAll = true }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete all")
                    }
                }
            )
        }
    ) { padding ->
        if (jokes.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No rated jokes yet.", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Rate a few jokes to see them here. You can save, share, or delete them.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                grouped.forEach { (rating, list) ->
                    stickyHeader {
                        Surface(
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (rating) {
                                        5 -> "⭐️⭐️⭐️⭐️⭐️ ROFL"
                                        4 -> "⭐️⭐️⭐️⭐️ Chuckles"
                                        3 -> "⭐️⭐️⭐️ Not bad"
                                        2 -> "⭐️⭐️ Meh"
                                        else -> "⭐️ Oof"
                                    },
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(Modifier.weight(1f))
                                Text("${list.size}", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    items(
                        items = list,
                        key = { it.id to it.setup } // stable-ish key
                    ) { joke ->
                        RatedJokeRow(
                            joke = joke,
                            onSave = { saveTarget = joke },
                            onShare = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "${joke.setup} ${joke.punchline}")
                                }
                                context.startActivity(
                                    Intent.createChooser(shareIntent, "Share joke via:")
                                )
                            },
                            onDelete = { deleteTarget = joke }
                        )
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }

    // Save-to-people dialog
    if (saveTarget != null) {
        SaveToPeopleDialog(
            existingPeople = existingPeople,
            onDismiss = { saveTarget = null },
            onSave = { people ->
                saveTarget?.let { j ->
                    viewModel.saveJokeToPeople(j, people)
                    Toast.makeText(
                        context,
                        "Saved for ${people.joinToString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                saveTarget = null
            }
        )
    }

    // Confirm delete one
    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete this rated joke?") },
            text = {
                Text(
                    "This will remove the rating entry. " +
                            "Saved copies to people are unaffected."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    deleteTarget?.let { viewModel.deleteRatedJoke(it) }
                    deleteTarget = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }

    // Confirm delete all
    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            title = { Text("Delete all rated jokes?") },
            text = {
                Text(
                    "This removes all ratings. " +
                            "Saved jokes you sent to people will remain."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllRatedJokes()
                    confirmDeleteAll = false
                }) { Text("Delete All") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAll = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun RatedJokeRow(
    joke: Joke,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = joke.setup,
                style = MaterialTheme.typography.titleMedium
            )
            if (joke.punchline.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = joke.punchline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getRatingMessage(joke.rating),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(onClick = onSave) {
                        Icon(Icons.Default.Person, contentDescription = "Save to people")
                    }
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
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


