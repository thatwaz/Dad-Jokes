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

private fun String.normalizeQuotesAndSpaces(): String = this
    .replace('“', '"').replace('”', '"')
    .replace('‘', '\'').replace('’', '\'')
    // normalize weird spaces (NBSP, thin/narrow spaces)
    .replace('\u00A0', ' ')
    .replace('\u202F', ' ')
    .replace('\u2009', ' ')
    .replace('\u200A', ' ')
    .replace('\u2007', ' ')
    .trim()

private fun String.trimStrayQuotes(): String =
    this.trim().trim('"', '\'', '“', '”', '‘', '’', ')', ']', '}').trim()

private fun String.ensureEndsWithSentencePunct(): String =
    if (isEmpty()) this else if (last() in ".!?") this else this + "."


// --- The splitter: returns (setup, punchline) ---
private fun deriveSetupAndPunchline(raw: String): Pair<String, String> {
    val t = raw.normalizeQuotesAndSpaces()

    // 1) Explicit separators first (most reliable for one-liners)
    val seps = listOf("\n\n", "\n", " — ", " – ", " - ", "—", "–", ": ")
    for (sep in seps) {
        val idx = t.indexOf(sep)
        if (idx >= 0) {
            val left  = t.substring(0, idx).trim().ensureEndsWithSentencePunct()
            val right = t.substring(idx + sep.length).trimStrayQuotes()
            if (right.isNotBlank()) return left to right
        }
    }

    // 2) Sentence boundary:
    //    Split after the FIRST . ? !, allowing optional quotes/parens and ZERO OR MORE spaces
    //    Examples handled:
    //      "Why ...?Because ..."       (no space)
    //      "Why ...?\" Because ..."    (closing quote then space)
    //      "Why ...!  Then ..."        (multiple spaces)
    val m = Regex("""^(.+?[.?!])\s*["'”’)\]]*\s*(.+)$""").find(t)
    if (m != null) {
        val left  = m.groupValues[1].trim().ensureEndsWithSentencePunct()
        val right = m.groupValues[2].trimStrayQuotes()
        if (right.isNotBlank()) return left to right
    }

    // 3) (Optional extra safety) If we still didn't split, try a raw '?' without spaces.
    val q = t.indexOf('?')
    if (q >= 0 && q + 1 < t.length) {
        val left  = t.substring(0, q + 1).trim().ensureEndsWithSentencePunct()
        val right = t.substring(q + 1).trimStrayQuotes()
        if (right.isNotBlank()) return left to right
    }

    // 4) No reasonable split → treat as single line (no reveal)
    return t.ensureEndsWithSentencePunct() to ""
}


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

    // StateFlow flags
    val canBack by viewModel.canGoBack.collectAsState()
    val canForward by viewModel.canGoForward.collectAsState()

    // First joke on screen entry  ----------------------------- NEW
    LaunchedEffect(Unit) { viewModel.loadNext() }

    // Footer quips are banner-only
    var bannerMood by remember { mutableStateOf(StickMood.Idle) }

    // After AdPost returns, advance using loadNext() ---------- CHANGED
    val afterAdFetchFlow = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("afterAdFetch", false)
    val afterAdFetch by (afterAdFetchFlow?.collectAsState() ?: remember { mutableStateOf(false) })
    LaunchedEffect(afterAdFetch) {
        if (afterAdFetch) {
            navController.currentBackStackEntry?.savedStateHandle?.set("afterAdFetch", false)
            viewModel.loadNext() // was showNextJoke()
        }
    }

    // Count Next taps to trigger ad flow every 5
    var nextTapCount by remember { mutableStateOf(0) }

    // ---- layout constants ----
    val seatsHeight = 80.dp
    val gapBetweenBannerAndSeats = 16.dp
    val quipReserve = 48.dp
    val bannerHeight = if (adsEnabled) 50.dp else 0.dp
    val footerReserve = seatsHeight + bannerHeight + gapBetweenBannerAndSeats + quipReserve + 12.dp
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

    // Reset per-joke state when the joke content changes
    LaunchedEffect(jokeState?.setup, jokeState?.punchline) {
        isPunchlineRevealed = false
        typingDone = false
        bannerMood = StickMood.Idle
        pendingBannerMood = null
    }

    // Local helper to reset transient UI state when navigating
    val resetForNav: () -> Unit = {
        isPunchlineRevealed = false
        typingDone = false
        bannerMood = StickMood.Idle
        pendingBannerMood = null
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
                val apiPunch = joke.punchline.normalizeQuotesAndSpaces().trimStrayQuotes()
                val (derivedSetup, derivedPunch) = deriveSetupAndPunchline(joke.setup)

                val displaySetup = derivedSetup
                val displayPunchline = if (apiPunch.isNotBlank()) apiPunch else derivedPunch

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
                    }
                ) { Text("Previous") }

                Button(
                    onClick = {
                        resetForNav()
                        nextTapCount += 1
                        val shouldStartAdFlow = adsEnabled && (nextTapCount % 5 == 0)
                        if (shouldStartAdFlow) {
                            navController.navigate(NavRoutes.AdPre.route)
                        } else {
                            viewModel.loadNext() // was showNextJoke()  ---- CHANGED
                        }
                    }
                ) {
                    Text("Next Joke")
                }
            }
        }

        // ===== Footer: Banner -> Quip -> Seats =====
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





























