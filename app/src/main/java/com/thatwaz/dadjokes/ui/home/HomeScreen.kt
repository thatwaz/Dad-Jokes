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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

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
    val activity = remember { context.findActivity() }

    var isPunchlineRevealed by remember { mutableStateOf(false) }
    var typingDone by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    val existingPeople by viewModel.peopleNames.collectAsState()

    // Footer quips are banner-only
    var bannerMood by remember { mutableStateOf(StickMood.Idle) }

    // After AdPost returns, fetch a new joke
    val afterAdFetchFlow = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("afterAdFetch", false)
    val afterAdFetch by (afterAdFetchFlow?.collectAsState() ?: remember { mutableStateOf(false) })
    LaunchedEffect(afterAdFetch) {
        if (afterAdFetch) {
            navController.currentBackStackEntry?.savedStateHandle?.set("afterAdFetch", false)
            viewModel.fetchJoke()
        }
    }

    // Count Next taps to trigger ad flow every 5
    var nextTapCount by remember { mutableStateOf(0) }

    // ---- layout constants ----
    val seatsHeight = 80.dp
    val gapBetweenBannerAndSeats = 16.dp

    // ⬆️ More room so quips never clip; still reserves space so banner won’t drop.
    val quipReserve = 48.dp

    val bannerHeight = if (adsEnabled) 50.dp else 0.dp
    val footerReserve = seatsHeight + bannerHeight + gapBetweenBannerAndSeats + quipReserve + 12.dp

    // Stable area for setup + optional punchline
    val setupAreaMinHeight = 200.dp

    // Delay banner quip until typing finishes
    var pendingBannerMood by remember { mutableStateOf<StickMood?>(null) }
    LaunchedEffect(typingDone, pendingBannerMood) {
        val m = pendingBannerMood
        if (typingDone && m != null) {
            kotlinx.coroutines.delay(900)
            bannerMood = m
            pendingBannerMood = null
        }
    }

    // Reset per-joke state when joke changes
    LaunchedEffect(jokeState?.setup, jokeState?.punchline) {
        isPunchlineRevealed = false
        typingDone = false
        bannerMood = StickMood.Idle
        pendingBannerMood = null
    }

    Box(Modifier.fillMaxSize()) {

        // ===== Main content (centered in available space above footer) =====
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(
                    top = 8.dp,
                    bottom = footerReserve  // keep clear of footer overlay
                ),
            verticalArrangement = Arrangement.Center,    // ⬅️ center instead of top
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            jokeState?.let { joke ->
                val (displaySetup, displayPunchline) = when {
                    joke.setup.contains("?") -> joke.setup to joke.punchline
                    joke.setup.contains(".") -> {
                        val setup = joke.setup.substringBefore(".").trim() + "."
                        val punch = joke.setup.substringAfter(".").trim()
                        setup to punch
                    }
                    else -> joke.setup to joke.punchline
                }

                // Stable region prevents bounce; sits closer to center now
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
                }

                Spacer(Modifier.height(28.dp))   // ⬅️ slight nudge closer to center before buttons
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    enabled = viewModel.canGoBack(),
                    onClick = {
                        isPunchlineRevealed = false
                        typingDone = false
                        bannerMood = StickMood.Idle
                        pendingBannerMood = null
                        viewModel.showPreviousJoke()
                    }
                ) { Text("Previous") }

                Button(
                    onClick = {
                        isPunchlineRevealed = false
                        typingDone = false
                        bannerMood = StickMood.Idle
                        pendingBannerMood = null

                        nextTapCount += 1
                        val shouldStartAdFlow = adsEnabled && (nextTapCount % 5 == 0)
                        if (shouldStartAdFlow) {
                            navController.navigate(NavRoutes.AdPre.route)
                        } else {
                            viewModel.fetchJoke()
                        }
                    }
                ) { Text("Next Joke") }
            }
        }

        // ===== Footer: Banner -> Quip (min height) -> Seats =====
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

            // ⬅️ Was .height(...). Now .heightIn(min = quipReserve) so taller bubbles aren’t clipped.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = quipReserve),
                contentAlignment = Alignment.Center
            ) {
                QuipBubble(
                    mood = bannerMood,
                    allowed = setOf(StickMood.BannerLoaded, StickMood.BannerFailed, StickMood.Clicked)
                )
            }

            TheaterStickmenImage(
                modifier = Modifier.fillMaxWidth(),
                heightDp = seatsHeight
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


























