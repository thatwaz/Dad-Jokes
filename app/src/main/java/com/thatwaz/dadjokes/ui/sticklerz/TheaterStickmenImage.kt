// TheaterStickmenImage.kt
package com.thatwaz.dadjokes.ui.sticklerz

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thatwaz.dadjokes.R

@Composable
fun TheaterStickmenImage(
    modifier: Modifier = Modifier,
    @DrawableRes resId: Int = R.drawable.theater_stickmen,
    heightDp: Dp = 80.dp    // smaller so nothing gets near the banner
) {
    Image(
        painter = painterResource(resId),
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp),
        // Show the entire image (no cropping)
        contentScale = ContentScale.Fit,
        alignment = Alignment.BottomCenter,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
    )
}




