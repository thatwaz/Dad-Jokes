package com.thatwaz.dadjokes.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay

@Composable
fun TypewriterText(
    fullText: String,
    typingSpeedMs: Long = 40L, // You can speed up or slow down here
    onTypingComplete: () -> Unit = {}
) {
    var visibleText by remember { mutableStateOf("") }

    LaunchedEffect(fullText) {
        visibleText = ""
        for (i in fullText.indices) {
            visibleText += fullText[i]
            delay(typingSpeedMs)
        }
        onTypingComplete()
    }

    Text(
        text = visibleText,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
    )
}
