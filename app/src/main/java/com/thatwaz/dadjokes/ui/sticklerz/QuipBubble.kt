package com.thatwaz.dadjokes.ui.sticklerz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// If StickMood already lives in its own file, delete this duplicate.
enum class StickMood { Idle, BannerLoaded, BannerFailed, InterstitialReady, InterstitialShown, InterstitialDismissed, Clicked }

@Composable
fun QuipBubble(
    mood: StickMood,
    modifier: Modifier = Modifier,
    showMs: Long = 8_000L,
    allowed: Set<StickMood> = emptySet(),

    // ✨ Styling knobs (theme-aware defaults)
    containerColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    elevation: Dp = 6.dp
) {
    var visible by remember { mutableStateOf(false) }
    var line by remember { mutableStateOf("") }

    LaunchedEffect(mood, allowed) {
        if (mood !in allowed || mood == StickMood.Idle) {
            visible = false
            return@LaunchedEffect
        }
        val picked = Quips.randomFor(mood)
        if (picked != null) {
            line = picked
            visible = true
            kotlinx.coroutines.delay(showMs)
            visible = false
        } else {
            visible = false
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
        exit  = fadeOut() + slideOutVertically(targetOffsetY = { it / 3 }),
        modifier = modifier
    ) {
        Surface(
            color = containerColor,
            contentColor = contentColor,
            shape = RoundedCornerShape(14.dp),
            tonalElevation = elevation,
            shadowElevation = elevation,
            border = BorderStroke(1.dp, borderColor.copy(alpha = 0.35f)),
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .heightIn(min = 44.dp) // 👈 never too short
        ) {
            Text(
                text = line,
                style = MaterialTheme.typography.titleSmall, // bigger than body
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .fillMaxWidth()
            )
        }
    }
}


//@Composable
//fun QuipBubble(
//    mood: StickMood,
//    modifier: Modifier = Modifier,
//    showMs: Long = 10_000L,
//    allowed: Set<StickMood> = emptySet() // explicit opt-in
//) {
//    val picker = remember { CooldownPickerByValue(window = 10) }
//    var visible by remember { mutableStateOf(false) }
//    var line by remember { mutableStateOf("") }
//
//    LaunchedEffect(mood, allowed) {
//        if (mood !in allowed || mood == StickMood.Idle) {
//            visible = false
//            return@LaunchedEffect
//        }
//
//        // Get the pool for this mood (helper shown below)
//        val pool = Quips.poolFor(mood)
//        if (pool.isEmpty()) {
//            visible = false
//            return@LaunchedEffect
//        }
//
//        // Use separate buckets so banner & interstitial have independent cooldowns
//        val bucketKey = if (mood.isBannerMood()) "banner" else "interstitial"
//
//        line = picker.pick(pool, key = bucketKey)
//        visible = true
//        kotlinx.coroutines.delay(showMs)
//        visible = false
//    }
//
//    AnimatedVisibility(
//        visible = visible,
//        enter = fadeIn(),
//        exit = fadeOut(),
//        modifier = modifier
//    ) {
//        Box(
//            modifier = Modifier
//                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
//                .padding(horizontal = 12.dp, vertical = 8.dp)
//        ) {
//            Text(
//                line,
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurface,
//                textAlign = TextAlign.Center
//            )
//        }
//    }
//}

// Small helpers (keep in same file or a utils file)
private fun StickMood.isBannerMood() = when (this) {
    StickMood.BannerLoaded, StickMood.BannerFailed, StickMood.Clicked -> true
    else -> false
}






//@Composable
//fun QuipBubble(
//    mood: StickMood,
//    modifier: Modifier = Modifier,
//    showMs: Long = 6000L,
//    allowed: Set<StickMood> = StickMood.values().toSet() // 👈 filter which moods can trigger this bubble
//) {
//    var visible by remember { mutableStateOf(false) }
//    var line by remember { mutableStateOf("") }
//
//    // ✨ Quip libraries (edit/add as you like)
//    val bannerLoaded = listOf(
//        "Our sponsor insisted.", "Capitalism rectangle acquired.", "Ad loaded. Behave.",
//        "Behold, the rectangle of revenue.", "Sponsored pixels detected.", "Ah yes, commerce.",
//        "This keeps the dad jokes free(-ish).", "Please clap (quietly).", "Try not to stare directly at it.",
//        "It pays for the punchlines.", "Ad’s here; act natural.", "Don’t worry, it’s housetrained.",
//        "I bet my cat could use that!",
//        "Is anyone working on the Y3K issue?",
//        "Finally, a rectangle that believes in me.",
//        "Ah yes, my next impulsive life choice.",
//        "Bold of it to assume I have money.",
//        "Put it on my imaginary credit card.",
//        "This ad passed the vibe check. Barely.",
//        "The prophecy foretold a rectangle.",
//        "Adding to cart (spiritually).",
//        "Economy: boosted. Wallet: ghosted.",
//        "At last, a solution to problems I don’t have.",
//        "New fear unlocked: targeted rectangles.",
//        "I, too, identify as ‘limited-time offer’.",
//        "Look, it’s capitalism in 1080p.",
//        "Finally, something my toaster can’t run.",
//        "This ad was raised by wolves.",
//        "Clinically proven* (*by the ad).",
//        "Now in flavors you can’t afford.",
//        "I felt that in my subscription fatigue.",
//        "May cause side effects like… shopping.",
//        "Probably tax-deductible in Narnia.",
//        "Recommended by 9/10 imaginary experts.",
//        "Because your other apps weren’t judging you enough.",
//        "Simply must-have, said the rectangle.",
//        "It pairs well with poor decisions.",
//        "Yes, I absolutely needed another hobby.",
//        "Minimalist design. Maximalist price.",
//        "Un-skippable in spirit.",
//        "Finally, an ad that understands my chaos.",
//        "Now with extra ‘upgrade to premium’ energy.",
//        "I’ve seen less persistent pop-ups in folklore.",
//        "Perfect for people who say ‘let’s circle back’.",
//        "Certified fresh by the Council of Rectangles.",
//        "Somewhere, a marketer just fist-pumped.",
//        "My cat already added it to cart.",
//        "Runs on vibes and microtransactions.",
//        "It’s giving… ‘limited supply’.",
//        "This is what my algorithms think of me.",
//        "The rectangle demands attention.",
//        "I’ll buy it when my plants start paying rent."
//    )
//    val bannerFailed = listOf(
//        "Ad ghosted us.", "No ad? Free air time!", "Server said 'nah'.",
//        "Budget ran off for milk.", "Invisible sponsor. Very exclusive.",
//        "Ad took a personal day.", "It’s buffering… in spirit.", "Silence. Sponsored by no one.",
//        "Ad ghosted us. Respect.",
//        "No ad? Behold: free pixels.",
//        "Even the sponsor needed a coffee.",
//        "Budget went out for milk.",
//        "Invisible ad—very premium.",
//        "It’s buffering… in a parallel universe.",
//        "Ad called in ‘meh’.",
//        "Sponsored by silence™.",
//        "The rectangle took a personal day.",
//        "404: Capitalism not found.",
//        "I guess we’re the content now.",
//        "Marketing blinked first.",
//        "Plot twist: we’re ad-free (for 10 seconds).",
//        "Ad went to chase its KPIs.",
//        "Paperwork said ‘come back later’.",
//        "The economy unplugged itself."
//    )
//    val interstitialReady = listOf(
//        "The projector is warming up its capitalism reels.",
//        "Please secure your popcorn; marketing turbulence ahead.",
//        "Intermission approaching—stretch your sarcasm muscles.",
//        "Trailers of commerce begin shortly. No autographs.",
//        "A tasteful pause for the rectangle arts.",
//        "Stand by: persuasion attempting to load charm module.",
//        "Soon: a message from our rectangle overlords.",
//        "We preheated the ad to 375° for optimal impressions.",
//        "This break brought to you by ‘Probably Relevant, Maybe’.",
//        "Brace for impact—of limited-time opinions.",
//        "The lobby smells like metrics and nachos.",
//        "Hold my root beer, I’m announcing capitalism.",
//        "Micro-nap now; we’ll wake you for the punchline.",
//        "We’re buffering something shiny and budget-approved.",
//        "A brief word from the Department of Urgent Wants.",
//        "Hush now, the rectangle is practicing its vowels.",
//        "Prepare your best nod-like-you-understand face.",
//        "Incoming: an offer that thinks it’s destiny.",
//        "Please refrain from adopting the ad as a lifestyle.",
//        "Warming up the ‘Do I need this?’ generator.",
//        "Someone ironed a suit onto this message.",
//        "We interrupt these groans for a fiscal moment.",
//        "Marketing has entered the chat wearing sunglasses.",
//        "Intermission: brought to you by the letter $ and the number ‘maybe’.",
//        "Your patience will be rewarded with… opinions.",
//        "Cue the soft jazz and assertive fonts.",
//        "Pop quiz: what even is a ‘limited run’? (No wrong answers.)",
//        "This rectangle trained for months to be two-dimensional.",
//        "Please pet the ad with your eyes only.",
//        "Thrilling previews of products you didn’t know existed.",
//        "We’ve summoned a sponsored omen. It looks rectangular.",
//        "Prepare to be gently influenced at medium heat.",
//        "Take a deep breath. Inhale Helvetica. Exhale ROI."
//    )
//
//    val interstitialShown = listOf(
//        "Remain calm. The rectangle is doing its ritual.",
//        "Temporary capitalism storm—hold onto your popcorn.",
//        "Please enjoy this scheduled stare at commerce.",
//        "If overwhelmed, blink in Morse code.",
//        "Do not adjust your sarcasm. We already did.",
//        "Somewhere, a KPI just smiled.",
//        "Sponsored eclipse in progress. Sunscreen optional.",
//        "This rectangle claims it can change your life. Bold.",
//        "We put the ‘ad’ in ‘adventure’ (legally distinct).",
//        "I taught this rectangle to roll over. It monetized instead.",
//        "Loading… opinions you didn’t know you had.",
//        "Try focusing on the upper-left pixel. Meditative.",
//        "The algorithm thinks this is your vibe. The algorithm is cheeky.",
//        "My cat is taking notes for a purchase it won’t make.",
//        "This message brought to you by rectangles who care (about metrics).",
//        "Imagine elevator music, but ambitious.",
//        "Behold: persuasion in 2D.",
//        "We now pause reality for a word from the void.",
//        "Please keep hands and feet inside the attention span.",
//        "This ad has ambitions. I respect that.",
//        "Scientifically engineered to be… present.",
//        "Just nod like you’re learning something.",
//        "Limited-time offer: your patience.",
//        "The rectangle and I are in a staring contest.",
//        "Pretend you’re reading a very tiny novel.",
//        "It pairs nicely with existential dread.",
//        "Somewhere, a marketer just whispered, ‘conversion’."
//    )
//
//    val interstitialDismissed = listOf(
//        "Congratulations: you closed a rectangle.",
//        "Welcome back. We kept your groans on pause.",
//        "The intermission has left the chat.",
//        "Plot resumed. Previously, on Dad Jokes…",
//        "You survived with all major organs, including sarcasm.",
//        "The economy thanks you for your patience.",
//        "Achievement unlocked: Ad Escape Artist.",
//        "And we’re clear—cue the punchlines.",
//        "Your attention has returned from its sponsored vacation.",
//        "Refreshments are imaginary; jokes are real.",
//        "Back to low-budget comedy, high-budget puns.",
//        "We now return to our regularly scheduled eye rolls.",
//        "I filed the ad under ‘character development’.",
//        "No spoilers, but the rectangle had a twist ending.",
//        "Your seat warmed itself out of anxiety.",
//        "Award for endurance: one (1) dad joke.",
//        "Intermission over. Pretend nothing happened.",
//        "Thanks for holding. Your pun will be with you shortly.",
//        "Tell no one of what you saw. Or tell everyone.",
//        "We tested your patience. It passed with a smirk.",
//        "Rebooting humor subsystem… okay, it’s still dad jokes.",
//        "Wallet status: unchanged. Spirit: slightly weirder.",
//        "You lost 20 seconds, gained a story.",
//        "The rectangle ascended. We remain.",
//        "And now, a joke so good it almost pays rent.",
//        "Back from the ad mines, covered in glitter.",
//        "Resume groaning on my mark… groan."
//    )
//
//    val clicked = listOf(
//        "Bold tap.", "Curiosity won.", "Hope it’s worth it.", "You poked the sponsor.",
//        "Adventure awaits (maybe).", "Mind the fine print.", "Report back with snacks.", "Return when ready for puns."
//    )
//
//    LaunchedEffect(mood, allowed) {
//        // 🚦 If this mood isn't allowed for this bubble, hide and bail.
//        if (mood !in allowed || mood == StickMood.Idle) {
//            visible = false
//            return@LaunchedEffect
//        }
//
//        val pool = when (mood) {
//            StickMood.BannerLoaded -> bannerLoaded
//            StickMood.BannerFailed -> bannerFailed
//            StickMood.InterstitialReady -> interstitialReady
//            StickMood.InterstitialShown -> interstitialShown
//            StickMood.InterstitialDismissed -> interstitialDismissed
//            StickMood.Clicked -> clicked
//            else -> emptyList()
//        }
//
//        if (pool.isNotEmpty()) {
//            line = pool.random()
//            visible = true
//            delay(showMs)
//            visible = false
//        } else {
//            visible = false
//        }
//    }
//
//    AnimatedVisibility(
//        visible = visible,
//        enter = fadeIn(),
//        exit = fadeOut(),
//        modifier = modifier
//    ) {
//        Box(
//            modifier = Modifier
//                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
//                .padding(horizontal = 12.dp, vertical = 8.dp)
//        ) {
//            Text(
//                line,
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurface,
//                textAlign = TextAlign.Center
//            )
//        }
//    }
//}





