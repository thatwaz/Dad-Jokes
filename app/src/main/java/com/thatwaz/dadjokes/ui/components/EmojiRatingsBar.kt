package com.thatwaz.dadjokes.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun EmojiRatingBar(
    selectedRating: Int,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val emojis = listOf("😒", "😐", "🙂", "😆", "🤣")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        emojis.forEachIndexed { index, emoji ->
            val isSelected = selectedRating == index + 1

            // Animate dot color and size
            val dotColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                label = "DotColorAnim"
            )

            val dotSize by animateDpAsState(
                targetValue = if (isSelected) 12.dp else 6.dp,
                label = "DotSizeAnim"
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Dot above emoji (animated)
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .background(color = dotColor, shape = CircleShape)
                )

                Text(
                    text = emoji,
                    fontSize = 28.sp,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { onRatingSelected(index + 1) }
                )
            }
        }
    }
}

