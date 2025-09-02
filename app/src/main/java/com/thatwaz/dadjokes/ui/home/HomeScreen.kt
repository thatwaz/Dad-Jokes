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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

    // Stick-quip mood
//    var stickMood by remember { mutableStateOf(StickMood.Idle) }
    var bannerMood by remember { mutableStateOf(StickMood.Idle) }
    var interstitialMood by remember { mutableStateOf(StickMood.Idle) }

    // Interstitial holder + callbacks
    val interstitial = remember { InterstitialHolder(context) }
    LaunchedEffect(Unit) { interstitial.load() }
    DisposableEffect(interstitial) {
        interstitial.onReady = { interstitialMood = StickMood.InterstitialReady }
        interstitial.onShown = { interstitialMood = StickMood.InterstitialShown }
        interstitial.onDismissed = { interstitialMood = StickMood.InterstitialDismissed }
        interstitial.onLoadFailed = { interstitialMood = StickMood.BannerFailed }
        onDispose { }
    }

    var nextTapCount by remember { mutableStateOf(0) }

    // ---- layout constants (single source of truth) ----
    val seatsHeight = 80.dp                  // tweak (72–88.dp)
    val gapBetweenBannerAndSeats = 16.dp
    val quipReserve = 32.dp
    val bannerHeight = if (adsEnabled) 50.dp else 0.dp
    val footerReserve = seatsHeight + bannerHeight + gapBetweenBannerAndSeats + quipReserve + 12.dp

    Box(Modifier.fillMaxSize()) {

        // ===== Top-aligned main content (scrollable) =====
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = footerReserve), // keep away from footer
            verticalArrangement = Arrangement.Top,
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
                            color = androidx.compose.ui.graphics.Color.Gray,
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
                    onClick = { showSaveDialog = true },
                    modifier = Modifier.padding(top = 12.dp)
                ) {
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

                Spacer(Modifier.height(24.dp))

            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    enabled = viewModel.canGoBack(),
                    onClick = {
                        isPunchlineRevealed = false
                        typingDone = false
                        viewModel.showPreviousJoke()
                    }
                ) { Text("Previous") }

                Button(
                    onClick = {
                        isPunchlineRevealed = false
                        typingDone = false
                        nextTapCount += 1

                        val shouldShowInterstitial = adsEnabled && (nextTapCount % 5 == 0)
                        if (shouldShowInterstitial && activity != null && interstitial.isReady()) {
                            interstitial.show(activity) { viewModel.fetchJoke() }
                        } else {
                            viewModel.fetchJoke()
                        }
                    }
                ) { Text("Next Joke") }
            }

            if (adsEnabled) {
                TextButton(onClick = { activity?.let { billingVM.launchRemoveAdsPurchase(it) } }) {
                    Text("Remove Ads")
                }
            } else {
                Text(
                    text = "Ads removed ✓",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // ===== Bottom footer: Banner -> Quip -> Seats (no overlap) =====
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (adsEnabled) {
                BannerAdSimple(
                    modifier = Modifier.fillMaxWidth(),
                    onLoaded = { bannerMood = StickMood.BannerLoaded },
                    onFailed = { bannerMood = StickMood.BannerFailed },
                    onClicked = { bannerMood = StickMood.Clicked }
                )
                Spacer(Modifier.height(gapBetweenBannerAndSeats))
            }

            QuipBubble(
                mood = bannerMood,
                allowed = setOf(
                    StickMood.BannerLoaded,
                    StickMood.BannerFailed,
                    StickMood.Clicked
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

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























