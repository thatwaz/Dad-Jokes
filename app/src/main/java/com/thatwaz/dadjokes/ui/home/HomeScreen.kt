@file:Suppress("UnusedImport")

package com.thatwaz.dadjokes.ui.home

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.thatwaz.dadjokes.navigation.NavRoutes
import com.thatwaz.dadjokes.ui.ads.BannerAdSimple
import com.thatwaz.dadjokes.ui.ads.InterstitialHolder
import com.thatwaz.dadjokes.ui.components.EmojiRatingBar
import com.thatwaz.dadjokes.ui.components.TypewriterText
import com.thatwaz.dadjokes.ui.dialogs.SaveToPeopleDialog
import com.thatwaz.dadjokes.ui.sticklerz.QuipBubble
import com.thatwaz.dadjokes.viewmodel.BillingViewModel
import com.thatwaz.dadjokes.viewmodel.JokeViewModel
import com.thatwaz.dadjokes.ui.sticklerz.Sticklerz
import com.thatwaz.dadjokes.ui.sticklerz.StickMood
import com.thatwaz.dadjokes.ui.sticklerz.TheaterStickmen
import com.thatwaz.dadjokes.ui.sticklerz.TheaterStickmenImage

@RequiresApi(35)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: JokeViewModel = hiltViewModel(),
    billingVM: BillingViewModel = hiltViewModel()
) {
    val jokeState by viewModel.joke.collectAsState()
    val adsEnabled by billingVM.adsEnabled.collectAsState(initial = true)

    val context = LocalContext.current

    var isPunchlineRevealed by remember { mutableStateOf(false) }
    var typingDone by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    val existingPeople by viewModel.peopleNames.collectAsState()

    val canBack by viewModel.canGoBack.collectAsState()
    val canForward by viewModel.canGoForward.collectAsState()

    // ---- banner quips ----
    var bannerMood by remember { mutableStateOf(StickMood.Idle) }
    var pendingBannerMood by remember { mutableStateOf<StickMood?>(null) }

    // apply pending mood once typing finishes
    LaunchedEffect(typingDone, pendingBannerMood) {
        pendingBannerMood?.let { m ->
            if (typingDone) {
                kotlinx.coroutines.delay(900)
                bannerMood = m
                pendingBannerMood = null
            }
        }
    }

    // ---- cycle counter (1..5) used for interstitial + quips ----
    var jokesInCycle by rememberSaveable { mutableStateOf(0) }

    // helper to set quip for the current position in 5-pack
    fun setQuipForCycle(pos: Int) {
        val mood = when (pos) {
            1 -> StickMood.BatchFirst
            3 -> StickMood.BatchThird
            else -> StickMood.Idle
        }
        if (typingDone) bannerMood = mood else pendingBannerMood = mood
    }

    // first load counts as #1 in the cycle
    LaunchedEffect(Unit) {
        jokesInCycle = 1
        viewModel.loadNext()
        setQuipForCycle(1)
    }

    // reset per-joke UI when content changes
    LaunchedEffect(jokeState?.setup, jokeState?.punchline) {
        isPunchlineRevealed = false
        typingDone = false
        // keep current/pending mood; it’s set by setQuipForCycle()
    }

    // ---- layout constants ----
    val seatsHeight = 132.dp          // ⬅️ a little bigger
    val gapBetweenBannerAndSeats = 16.dp
    val quipHeight = 80.dp            // fixed height; prevents banner shifting
    val bannerHeight = if (adsEnabled) 50.dp else 0.dp
    val footerReserve = seatsHeight + bannerHeight + gapBetweenBannerAndSeats + quipHeight + 12.dp
    val setupAreaMinHeight = 200.dp

    // local helper to reset transient UI bits on navigation
    val resetForNav: () -> Unit = {
        isPunchlineRevealed = false
        typingDone = false
        // bannerMood/pending set by setQuipForCycle()
    }

    Box(Modifier.fillMaxSize()) {

        // ===== Main content =====
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, bottom = footerReserve),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            jokeState?.let { joke ->
                val displaySetup = joke.setup.trim()
                val displayPunchline = joke.punchline.trim()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = setupAreaMinHeight)
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TypewriterText(
                            fullText = displaySetup,
                            startDelayMillis = 500L,
                            onTypingComplete = { typingDone = true }
                        )

                        if (isPunchlineRevealed && displayPunchline.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = displayPunchline,
                                style = MaterialTheme.typography.bodyLarge,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center
                            )
                        } else if (displayPunchline.isNotBlank() && typingDone) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Tap to reveal punchline",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.clickable { isPunchlineRevealed = true }
                            )
                        }
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    IconButton(onClick = { showSaveDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Save to people",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = {
                            val shareText = buildString {
                                append(displaySetup)
                                if (displayPunchline.isNotBlank()) append(" ").append(displayPunchline)
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
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
                }

                Spacer(Modifier.height(28.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    enabled = canBack,
                    onClick = {
                        resetForNav()
                        viewModel.showPreviousJoke()
                        // don’t change cycle on back
                    }
                ) { Text("Previous") }

                Button(
                    onClick = {
                        resetForNav()

                        // advance cycle (1..5), wrap after 5
                        jokesInCycle = if (jokesInCycle >= 5) 1 else jokesInCycle + 1

                        val shouldStartAdFlow = adsEnabled && (jokesInCycle == 5)
                        if (shouldStartAdFlow) {
                            navController.navigate(NavRoutes.AdPre.route)
                        } else {
                            viewModel.loadNext()
                            setQuipForCycle(jokesInCycle) // quip on 1st and 3rd
                        }
                    }
                ) {
                    Text("Next Joke")
                }
            }
        }

        // ===== Footer: Banner -> Quip (fixed height) -> Seats =====
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (adsEnabled) {
                BannerAdSimple(
                    modifier = Modifier.fillMaxWidth(),
                    onLoaded = { pendingBannerMood = StickMood.BannerLoaded },
                    onFailed = {
                        bannerMood = StickMood.BannerFailed
                        pendingBannerMood = null
                    },
                    onClicked = {
                        bannerMood = StickMood.Clicked
                        pendingBannerMood = null
                    }
                )
                Spacer(Modifier.height(gapBetweenBannerAndSeats))
            }

            // fixed-height container => no banner jump
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(quipHeight)
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                QuipBubble(
                    mood = bannerMood,
                    allowed = setOf(
                        StickMood.BannerLoaded,
                        StickMood.BannerFailed,
                        StickMood.Clicked,
                        StickMood.BatchFirst,   // ⬅️ new
                        StickMood.BatchThird    // ⬅️ new
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            TheaterStickmenImage(
                modifier = Modifier.fillMaxWidth(),
                heightDp = seatsHeight      // ⬅️ bigger
            )
        }
    }

    if (showSaveDialog && jokeState != null) {
        SaveToPeopleDialog(
            existingPeople = existingPeople,
            onDismiss = { showSaveDialog = false },
            onSave = { people ->
                viewModel.saveCurrentJokeToPeople(people)
                Toast.makeText(context, "Saved for ${people.joinToString()}", Toast.LENGTH_SHORT).show()
                showSaveDialog = false
            }
        )
    }
}






























