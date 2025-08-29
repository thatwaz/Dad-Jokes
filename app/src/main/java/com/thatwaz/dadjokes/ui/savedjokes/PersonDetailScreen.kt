package com.thatwaz.dadjokes.ui.savedjokes



import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thatwaz.dadjokes.domain.model.SavedJokeDelivery
import com.thatwaz.dadjokes.ui.util.emojiFor
import com.thatwaz.dadjokes.viewmodel.PersonDetailViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    person: String,
    vm: PersonDetailViewModel = hiltViewModel()
) {
    val untold by vm.untold.collectAsState()
    val told by vm.told.collectAsState()

    val context = LocalContext.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var pickerForId by remember { mutableStateOf<Int?>(null) }
    var toDelete by remember { mutableStateOf<SavedJokeDelivery?>(null) }
    var showDeleteAll by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(person) },
                navigationIcon = {
                    IconButton(onClick = { backDispatcher?.onBackPressed() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteAll = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete all for $person")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ----- UNTOLD -----
            item {
                Text("Untold", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 4.dp))
                if (untold.isEmpty()) {
                    Text("No untold jokes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(untold, key = { it.id }) { j ->
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier
                            .padding(12.dp)
                            .animateContentSize()
                            .clickable { pickerForId = if (pickerForId == j.id) null else j.id }
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(j.setup, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Row {
                                IconButton(onClick = {
                                    shareJoke(context, j.setup, j.punchline)
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share")
                                }
                                IconButton(onClick = { toDelete = j }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                        if (pickerForId == j.id) {
                            Spacer(Modifier.height(8.dp))
                            EmojiPicker { rating ->
                                vm.markTold(j.id, rating)
                                pickerForId = null
                                // Show UNDO
                                scope.launch {
                                    val res = snackbarHostState.showSnackbar(
                                        message = "Marked told",
                                        actionLabel = "Undo",
                                        withDismissAction = true,
                                        duration = SnackbarDuration.Short
                                    )
                                    if (res == SnackbarResult.ActionPerformed) {
                                        vm.undoTold(j.id) // see ViewModel addition below
                                    }
                                }
                            }
                        } else {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Tap to log their reaction",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // divider
            item { Spacer(Modifier.height(12.dp)); Divider(); Spacer(Modifier.height(12.dp)) }

            // ----- TOLD -----
            item {
                Text("Told", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 4.dp))
                if (told.isEmpty()) {
                    Text("Nothing told yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(told, key = { it.id }) { j ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(j.setup, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Row {
                                IconButton(onClick = {
                                    shareJoke(context, j.setup, j.punchline)
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share")
                                }
                                IconButton(onClick = { toDelete = j }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                        j.reactionRating?.let { r ->
                            Spacer(Modifier.height(4.dp))
                            Text("Reaction: ${emojiFor(r)}", style = MaterialTheme.typography.labelMedium)
                        }
                        // Optional: Add an "Undo told" inline action as well
                        TextButton(
                            onClick = { vm.undoTold(j.id) },
                            modifier = Modifier.align(Alignment.End)
                        ) { Text("Move back to Untold") }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
    // --- Confirm delete single ---
    toDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete this joke?") },
            text = { Text("Remove this saved joke for $person? This can’t be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.delete(item)
                    toDelete = null
                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show() // ← use context
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancel") } }
        )
    }

// --- Confirm delete ALL ---
    if (showDeleteAll) {
        AlertDialog(
            onDismissRequest = { showDeleteAll = false },
            title = { Text("Delete all for $person?") },
            text = { Text("This will remove all saved jokes for this person (untold and told).") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteAllForPerson()
                    showDeleteAll = false
                    Toast.makeText(context, "All cleared for $person", Toast.LENGTH_SHORT).show() // ← use context
                }) { Text("Delete all") }
            },
            dismissButton = { TextButton(onClick = { showDeleteAll = false }) { Text("Cancel") } }
        )
    }


}

/* share helper */
private fun shareJoke(context: Context, setup: String, punchline: String?) {
    val text = if (!punchline.isNullOrBlank()) "$setup $punchline" else setup
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share via"))
}

@Composable
private fun EmojiPicker(onPick: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val options = listOf("😐", "🙂", "😆", "🤣", "😭")
        options.forEachIndexed { index, emoji ->
            AssistChip(onClick = { onPick(index + 1) }, label = { Text(emoji) })
        }
    }
}



