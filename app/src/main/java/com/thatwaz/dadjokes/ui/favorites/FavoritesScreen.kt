package com.thatwaz.dadjokes.ui.favorites



import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thatwaz.dadjokes.viewmodel.JokeViewModel

//@Composable
//fun FavoritesScreen(viewModel: JokeViewModel) {
//    val favorites by viewModel.favoriteJokes.collectAsState(initial = emptyList())
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Text(
//            text = "Favorite Jokes 🤩",
//            style = MaterialTheme.typography.headlineMedium,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//
//        if (favorites.isEmpty()) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(top = 32.dp),
//                contentAlignment = Alignment.TopCenter
//            ) {
//                Text("No favorites yet. Save a joke to see it here!")
//            }
//        } else {
//            val emojiLabels = mapOf(
//                1 to "😒 Oof. That one hurt",
//                2 to "😐 Meh",
//                3 to "🙂 Not bad!",
//                4 to "😆 That got a chuckle",
//                5 to "😂 ROFL!"
//            )
//
//            val jokesGroupedByRating = favorites.groupBy { it.rating }
//
//            LazyColumn(
//                verticalArrangement = Arrangement.spacedBy(12.dp),
//                modifier = Modifier.fillMaxSize()
//            ) {
//                jokesGroupedByRating
//                    .toSortedMap(compareByDescending { it }) // 5-star first
//                    .forEach { (rating, jokes) ->
//                        val label = emojiLabels[rating] ?: "🤔 Unrated"
//                        item {
//                            Text(
//                                text = label,
//                                style = MaterialTheme.typography.titleMedium,
//                                modifier = Modifier
//                                    .padding(vertical = 8.dp)
//                            )
//                        }
//                        items(jokes) { joke ->
//                            ElevatedCard(
//                                modifier = Modifier.fillMaxWidth()
//                            ) {
//                                Column(modifier = Modifier.padding(16.dp)) {
//                                    Text(
//                                        text = "${joke.setup} ${joke.punchline}".trim(),
//                                        style = MaterialTheme.typography.bodyLarge
//                                    )
//                                    if (rating > 0) {
//                                        Spacer(modifier = Modifier.height(8.dp))
//                                        Text(
//                                            text = "You rated this: ${emojiLabels[rating]?.take(2) ?: "🤔"}",
//                                            style = MaterialTheme.typography.labelSmall
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//            }
//        }
//    }
//}


