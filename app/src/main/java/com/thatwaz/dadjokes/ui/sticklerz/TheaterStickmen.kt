// app/src/main/java/com/thatwaz/dadjokes/ui/sticklerz/TheaterStickmen.kt
package com.thatwaz.dadjokes.ui.sticklerz

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun TheaterStickmen(
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .drawBehind {
                val strokeW = 5f
                val w = size.width
                val h = size.height

                // Floor line
                val floorY = h - strokeW
                drawLine(color, Offset(0f, floorY), Offset(w, floorY), strokeWidth = strokeW, cap = StrokeCap.Round)

                // Seat geometry
                val seatTop = h * 0.56f
                val seatHeight = h * 0.28f
                val seatRadius = 18f
                val gap = w * 0.06f
                val seatW = (w - gap * 3f) / 2f
                val leftX = gap
                val rightX = leftX + seatW + gap

                fun seat(left: Float) {
                    // Main seat box
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(left, seatTop),
                        size = androidx.compose.ui.geometry.Size(seatW, seatHeight),
                        cornerRadius = CornerRadius(seatRadius, seatRadius),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                    // Small "bump" on top of the seat back (headrest-ish)
                    val bumpW = seatW * 0.36f
                    val bumpH = seatHeight * 0.25f
                    val bumpLeft = left + seatW * 0.32f
                    val bumpTop = seatTop - bumpH * 0.65f
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(bumpLeft, bumpTop),
                        size = androidx.compose.ui.geometry.Size(bumpW, bumpH),
                        cornerRadius = CornerRadius(12f, 12f),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }

                seat(leftX)
                seat(rightX)

                // People: head + neck post centered on each seat
                fun person(centerX: Float) {
                    val headR = h * 0.115f
                    val headY = seatTop - headR * 1.55f
                    // Head
                    drawCircle(
                        color = color,
                        radius = headR,
                        center = Offset(centerX, headY),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                    // Neck post
                    val neckTop = Offset(centerX, headY + headR + 4f)
                    val neckBot = Offset(centerX, seatTop - seatHeight * 0.10f)
                    drawLine(color, neckTop, neckBot, strokeWidth = strokeW, cap = StrokeCap.Round)
                }

                val leftCenter = leftX + seatW / 2f
                val rightCenter = rightX + seatW / 2f
                person(leftCenter)
                person(rightCenter)
            }
    )
}




//package com.thatwaz.dadjokes.ui.sticklerz
//
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.drawBehind
//import androidx.compose.ui.geometry.CornerRadius
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.unit.dp
//
//
//@Composable
//fun TheaterStickmen(
//    modifier: Modifier = Modifier
//) {
//    val color = MaterialTheme.colorScheme.onSurface
//    Box(
//        modifier = modifier
//            .fillMaxWidth()
//            .height(110.dp) // visual height of the seats + heads
//            .drawBehind {
//                val strokeW = 6f
//                val w = size.width
//                val h = size.height
//
//                // Seat geometry
//                val seatTop = h * 0.55f
//                val seatHeight = h * 0.28f
//                val seatRadius = 18f
//
//                // Left/right seat rects
//                val seatGap = w * 0.04f
//                val seatWidth = (w - seatGap * 3f) / 2f
//                val leftSeatLeft = seatGap
//                val rightSeatLeft = leftSeatLeft + seatWidth + seatGap
//
//                // Draw seats (rounded rectangles)
//                drawRoundRect(
//                    color = color,
//                    topLeft = Offset(leftSeatLeft, seatTop),
//                    size = androidx.compose.ui.geometry.Size(seatWidth, seatHeight),
//                    cornerRadius = CornerRadius(seatRadius, seatRadius),
//                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
//                )
//                drawRoundRect(
//                    color = color,
//                    topLeft = Offset(rightSeatLeft, seatTop),
//                    size = androidx.compose.ui.geometry.Size(seatWidth, seatHeight),
//                    cornerRadius = CornerRadius(seatRadius, seatRadius),
//                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
//                )
//
//                // Armrest-ish little bump on each seat back
//                fun seatBumps(left: Float) {
//                    val bumpW = seatWidth * 0.22f
//                    val bumpH = seatHeight * 0.22f
//                    val top = seatTop - bumpH * 0.55f
//                    drawRoundRect(
//                        color = color,
//                        topLeft = Offset(left + seatWidth * 0.39f, top),
//                        size = androidx.compose.ui.geometry.Size(bumpW, bumpH),
//                        cornerRadius = CornerRadius(12f, 12f),
//                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
//                    )
//                }
//                seatBumps(leftSeatLeft)
//                seatBumps(rightSeatLeft)
//
//                // Two stickmen (heads above the seat bumps)
//                fun person(centerX: Float) {
//                    val headR = h * 0.10f
//                    val headY = seatTop - headR * 1.6f
//                    // Head
//                    drawCircle(
//                        color = color,
//                        radius = headR,
//                        center = Offset(centerX, headY),
//                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
//                    )
//                    // Neck
//                    val neckTop = Offset(centerX, headY + headR + 4f)
//                    val neckBot = Offset(centerX, seatTop - seatHeight * 0.22f)
//                    drawLine(color, neckTop, neckBot, strokeWidth = strokeW, cap = StrokeCap.Round)
//                    // Tiny shoulders
//                    drawLine(
//                        color,
//                        Offset(centerX - headR * 0.8f, neckBot.y),
//                        Offset(centerX + headR * 0.8f, neckBot.y),
//                        strokeWidth = strokeW, cap = StrokeCap.Round
//                    )
//                }
//
//                val leftCenter = leftSeatLeft + seatWidth / 2f
//                val rightCenter = rightSeatLeft + seatWidth / 2f
//                person(leftCenter)
//                person(rightCenter)
//
//                // Floor line
//                val floorY = h - strokeW
//                drawLine(color, Offset(0f, floorY), Offset(w, floorY), strokeWidth = strokeW, cap = StrokeCap.Round)
//            }
//    )
//}
