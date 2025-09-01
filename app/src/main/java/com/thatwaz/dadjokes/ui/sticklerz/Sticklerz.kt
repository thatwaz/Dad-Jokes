package com.thatwaz.dadjokes.ui.sticklerz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

//enum class StickMood {
//    Idle, BannerLoaded, BannerFailed, InterstitialReady, InterstitialShown, InterstitialDismissed, Clicked
//}

@Composable
fun Sticklerz(
    modifier: Modifier = Modifier,
    mood: StickMood
) {
    var showBubble by remember { mutableStateOf(false) }
    var line by remember { mutableStateOf("") }

    // Pose targets (leftArm to rightArm degrees)
    val targetArms = remember(mood) {
        when (mood) {
            StickMood.BannerLoaded          -> 55f to 10f      // wave
            StickMood.BannerFailed          -> 15f to -15f     // shrug
            StickMood.InterstitialReady     -> -45f to 5f      // point / prep
            StickMood.InterstitialShown     -> 85f to 85f      // cover eyes
            StickMood.InterstitialDismissed -> 25f to 25f      // cheer
            StickMood.Clicked               -> 65f to 65f      // surprised
            else                            -> 20f to 20f
        }
    }
    val jumpTarget = remember(mood) { if (mood == StickMood.InterstitialDismissed) (-10).dp else 0.dp }

    // Remove "label =" so it works on older Compose versions
    val leftArm by animateFloatAsState(targetValue = targetArms.first, animationSpec = tween(250))
    val rightArm by animateFloatAsState(targetValue = targetArms.second, animationSpec = tween(250))
    val verticalOffset by animateDpAsState(targetValue = jumpTarget, animationSpec = tween(220))

    // Quip lines
    LaunchedEffect(mood) {
        val msg = when (mood) {
            StickMood.BannerLoaded          -> pick("Our sponsor insisted.", "Capitalism rectangle acquired.", "Ad loaded. Behave.")
            StickMood.BannerFailed          -> pick("Ad ghosted us.", "No ad? Free air time!", "Server said 'nah'.")
            StickMood.InterstitialReady     -> pick("Intermission warming up.", "Brace yourselves…", "Large rectangle soon.")
            StickMood.InterstitialShown     -> pick("Don’t panic.", "Look away if shy.", "It’s temporary.")
            StickMood.InterstitialDismissed -> pick("You survived!", "Back to jokes!", "And we’re clear.")
            StickMood.Clicked               -> pick("Bold tap.", "Curiosity won.", "Hope it’s worth it.")
            else                            -> ""
        }
        if (msg.isNotEmpty()) {
            line = msg
            showBubble = true
            kotlinx.coroutines.delay(2000)
            showBubble = false
        }
    }
    val strokeColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .offset(y = verticalOffset)
            .sizeIn(minWidth = 140.dp)
    ) {
        // Stick figure drawing
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(100.dp)
                .drawBehind {
                    val strokeWidth = 6f
                    val color = strokeColor
                    val centerX = size.width / 2f
                    val headY = size.height * 0.20f
                    val headR = size.minDimension * 0.14f

                    // Head (uses Stroke DrawStyle)
                    drawCircle(
                        color = color,
                        radius = headR,
                        center = Offset(centerX, headY),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Body
                    val neck = Offset(centerX, headY + headR + 6f)
                    val hipY = size.height * 0.70f
                    val hips = Offset(centerX, hipY)
                    drawLine(color, neck, hips, strokeWidth = strokeWidth, cap = StrokeCap.Round)

                    // Legs
                    val legLen = size.height * 0.22f
                    drawLine(color, hips, polar(hips, legLen, 210f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(color, hips, polar(hips, legLen, -30f), strokeWidth = strokeWidth, cap = StrokeCap.Round)

                    // Arms (angles relative to horizontal)
                    val shoulders = Offset(centerX, (neck.y + hips.y) / 2f)
                    val armLen = size.width * 0.28f
                    drawLine(color, shoulders, polar(shoulders, armLen, 180f - leftArm), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(color, shoulders, polar(shoulders, armLen, rightArm), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                }
        )

        // Quip bubble
        AnimatedVisibility(
            visible = showBubble && mood != StickMood.Idle,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 64.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun polar(origin: Offset, length: Float, degrees: Float): Offset {
    val rad = Math.toRadians(degrees.toDouble())
    return Offset(
        x = origin.x + (length * cos(rad)).toFloat(),
        y = origin.y + (length * sin(rad)).toFloat()
    )
}
private fun pick(vararg s: String) = s[Random.nextInt(s.size)]

