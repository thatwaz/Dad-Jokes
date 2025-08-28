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
    typingSpeed: Long = 75L,
    startDelayMillis: Long = 300L, // <-- Add this line
    onTypingComplete: () -> Unit = {}
) {
    var displayedText by remember { mutableStateOf("") }

    LaunchedEffect(fullText) {
        displayedText = ""
        delay(startDelayMillis) // <-- This is the new delay before typing begins
        for (char in fullText) {
            displayedText += char
            delay(typingSpeed)
        }
        onTypingComplete()
    }

    Text(
        text = displayedText,
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center
    )
}

