package com.thatwaz.dadjokes.ui.sticklerz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class StickMood { Idle, BannerLoaded, BannerFailed, InterstitialReady, InterstitialShown, InterstitialDismissed, Clicked }



@Composable
fun QuipBubble(
    mood: StickMood,
    modifier: Modifier = Modifier,
    showMs: Long = 2200L
) {
    var visible by remember { mutableStateOf(false) }
    var line by remember { mutableStateOf("") }

    // ✨ Quip libraries (edit/add as you like)
    val bannerLoaded = listOf(
        "Our sponsor insisted.",
        "Capitalism rectangle acquired.",
        "Ad loaded. Behave.",
        "Behold, the rectangle of revenue.",
        "Sponsored pixels detected.",
        "Ah yes, commerce.",
        "This keeps the dad jokes free(-ish).",
        "Please clap (quietly).",
        "Try not to stare directly at it.",
        "It pays for the punchlines.",
        "Ad’s here; act natural.",
        "Don’t worry, it’s housetrained."
    )

    val bannerFailed = listOf(
        "Ad ghosted us.",
        "No ad? Free air time!",
        "Server said 'nah'.",
        "Budget ran off for milk.",
        "Invisible sponsor. Very exclusive.",
        "Ad took a personal day.",
        "It’s buffering… in spirit.",
        "Silence. Sponsored by no one."
    )

    val interstitialReady = listOf(
        "Intermission warming up.",
        "Brace yourselves…",
        "Large rectangle soon.",
        "Popcorn optional.",
        "Incoming attention request.",
        "Stretch your scrolling finger.",
        "We preheated the capitalism.",
        "Showtime (almost)."
    )

    val interstitialShown = listOf(
        "Don’t panic.",
        "Look away if shy.",
        "It’s temporary.",
        "Breathe in… breathe ad.",
        "This too shall pass.",
        "A brief sponsored eclipse.",
        "Paging your patience.",
        "Covering eyes theatrically."
    )

    val interstitialDismissed = listOf(
        "You survived!",
        "Back to jokes!",
        "And we’re clear.",
        "Intermission over.",
        "Thanks for enduring.",
        "Now, more groans.",
        "Cue the punchlines.",
        "Freedom tastes punny."
    )

    val clicked = listOf(
        "Bold tap.",
        "Curiosity won.",
        "Hope it’s worth it.",
        "You poked the sponsor.",
        "Adventure awaits (maybe).",
        "Mind the fine print.",
        "Report back with snacks.",
        "Return when ready for puns."
    )

    LaunchedEffect(mood) {
        val pool = when (mood) {
            StickMood.BannerLoaded -> bannerLoaded
            StickMood.BannerFailed -> bannerFailed
            StickMood.InterstitialReady -> interstitialReady
            StickMood.InterstitialShown -> interstitialShown
            StickMood.InterstitialDismissed -> interstitialDismissed
            StickMood.Clicked -> clicked
            else -> emptyList()
        }
        if (pool.isNotEmpty()) {
            line = pool[Random.nextInt(pool.size)]
            visible = true
            delay(showMs)
            visible = false
        }
    }

    AnimatedVisibility(
        visible = visible && mood != StickMood.Idle,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                line,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}





