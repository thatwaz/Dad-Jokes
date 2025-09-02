package com.thatwaz.dadjokes.ui.sticklerz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class TheaterQuipMode { PreRoll, PostRoll }

@Composable
fun TheaterQuipScreen(
    mode: TheaterQuipMode,
    onPrimary: () -> Unit,           // Pre: “Continue” → show ad; Post: “Back to jokes”
    onSecondary: (() -> Unit)? = null, // optional “Skip” on Pre
    modifier: Modifier = Modifier,
    minImageHeightDp: Int = 240       // big PNG for the full-screen overlay
) {
    val preQuips = listOf(
        "Intermission warming up.", "Brace yourselves…", "Popcorn optional.",
        "Large rectangle soon.", "We preheated the capitalism.", "Showtime (almost)."
    )
    val postQuips = listOf(
        "You survived!", "Back to jokes!", "And we’re clear.",
        "Intermission over.", "Thanks for enduring.", "Cue the punchlines."
    )

    val line = remember(mode) {
        when (mode) {
            TheaterQuipMode.PreRoll -> preQuips.random()
            TheaterQuipMode.PostRoll -> postQuips.random()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
            Text(
                text = line,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = onPrimary) {
                Text(if (mode == TheaterQuipMode.PreRoll) "Continue" else "Back to jokes")
            }
            if (mode == TheaterQuipMode.PreRoll && onSecondary != null) {
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.TextButton(onClick = onSecondary) {
                    Text("Skip")
                }
            }
        }

        TheaterStickmenImage(
            modifier = Modifier.fillMaxWidth(),
            heightDp = minImageHeightDp.dp
        )
    }
}



