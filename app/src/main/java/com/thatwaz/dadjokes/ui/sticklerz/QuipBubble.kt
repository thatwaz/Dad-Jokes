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

// If StickMood already lives in its own file, delete this duplicate.
enum class StickMood { Idle, BannerLoaded, BannerFailed, InterstitialReady, InterstitialShown, InterstitialDismissed, Clicked }

@Composable
fun QuipBubble(
    mood: StickMood,
    modifier: Modifier = Modifier,
    showMs: Long = 6000L,
    allowed: Set<StickMood> = StickMood.values().toSet() // 👈 filter which moods can trigger this bubble
) {
    var visible by remember { mutableStateOf(false) }
    var line by remember { mutableStateOf("") }

    // ✨ Quip libraries (edit/add as you like)
    val bannerLoaded = listOf(
        "Our sponsor insisted.", "Capitalism rectangle acquired.", "Ad loaded. Behave.",
        "Behold, the rectangle of revenue.", "Sponsored pixels detected.", "Ah yes, commerce.",
        "This keeps the dad jokes free(-ish).", "Please clap (quietly).", "Try not to stare directly at it.",
        "It pays for the punchlines.", "Ad’s here; act natural.", "Don’t worry, it’s housetrained.",
        "I bet my cat could use that!",
        "Is anyone working on the Y3K issue?",
        "Finally, a rectangle that believes in me.",
        "Ah yes, my next impulsive life choice.",
        "Bold of it to assume I have money.",
        "Put it on my imaginary credit card.",
        "This ad passed the vibe check. Barely.",
        "The prophecy foretold a rectangle.",
        "Adding to cart (spiritually).",
        "Economy: boosted. Wallet: ghosted.",
        "At last, a solution to problems I don’t have.",
        "New fear unlocked: targeted rectangles.",
        "I, too, identify as ‘limited-time offer’.",
        "Look, it’s capitalism in 1080p.",
        "Finally, something my toaster can’t run.",
        "This ad was raised by wolves.",
        "Clinically proven* (*by the ad).",
        "Now in flavors you can’t afford.",
        "I felt that in my subscription fatigue.",
        "May cause side effects like… shopping.",
        "Probably tax-deductible in Narnia.",
        "Recommended by 9/10 imaginary experts.",
        "Because your other apps weren’t judging you enough.",
        "Simply must-have, said the rectangle.",
        "It pairs well with poor decisions.",
        "Yes, I absolutely needed another hobby.",
        "Minimalist design. Maximalist price.",
        "Un-skippable in spirit.",
        "Finally, an ad that understands my chaos.",
        "Now with extra ‘upgrade to premium’ energy.",
        "I’ve seen less persistent pop-ups in folklore.",
        "Perfect for people who say ‘let’s circle back’.",
        "Certified fresh by the Council of Rectangles.",
        "Somewhere, a marketer just fist-pumped.",
        "My cat already added it to cart.",
        "Runs on vibes and microtransactions.",
        "It’s giving… ‘limited supply’.",
        "This is what my algorithms think of me.",
        "The rectangle demands attention.",
        "I’ll buy it when my plants start paying rent."
    )
    val bannerFailed = listOf(
        "Ad ghosted us.", "No ad? Free air time!", "Server said 'nah'.",
        "Budget ran off for milk.", "Invisible sponsor. Very exclusive.",
        "Ad took a personal day.", "It’s buffering… in spirit.", "Silence. Sponsored by no one.",
        "Ad ghosted us. Respect.",
        "No ad? Behold: free pixels.",
        "Even the sponsor needed a coffee.",
        "Budget went out for milk.",
        "Invisible ad—very premium.",
        "It’s buffering… in a parallel universe.",
        "Ad called in ‘meh’.",
        "Sponsored by silence™.",
        "The rectangle took a personal day.",
        "404: Capitalism not found.",
        "I guess we’re the content now.",
        "Marketing blinked first.",
        "Plot twist: we’re ad-free (for 10 seconds).",
        "Ad went to chase its KPIs.",
        "Paperwork said ‘come back later’.",
        "The economy unplugged itself."
    )
    val interstitialReady = listOf(
        "Intermission warming up.", "Brace yourselves…", "Large rectangle soon.",
        "Popcorn optional.", "Incoming attention request.", "Stretch your scrolling finger.",
        "We preheated the capitalism.", "Showtime (almost).",

    )
    val interstitialShown = listOf(
        "Don’t panic.", "Look away if shy.", "It’s temporary.", "Breathe in… breathe ad.",
        "This too shall pass.", "A brief sponsored eclipse.", "Paging your patience.", "Covering eyes theatrically."
    )
    val interstitialDismissed = listOf(
        "You survived!", "Back to jokes!", "And we’re clear.", "Intermission over.",
        "Thanks for enduring.", "Now, more groans.", "Cue the punchlines.", "Freedom tastes punny."
    )
    val clicked = listOf(
        "Bold tap.", "Curiosity won.", "Hope it’s worth it.", "You poked the sponsor.",
        "Adventure awaits (maybe).", "Mind the fine print.", "Report back with snacks.", "Return when ready for puns."
    )

    LaunchedEffect(mood, allowed) {
        // 🚦 If this mood isn't allowed for this bubble, hide and bail.
        if (mood !in allowed || mood == StickMood.Idle) {
            visible = false
            return@LaunchedEffect
        }

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
            line = pool.random()
            visible = true
            delay(showMs)
            visible = false
        } else {
            visible = false
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
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





