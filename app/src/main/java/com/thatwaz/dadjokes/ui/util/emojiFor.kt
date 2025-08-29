package com.thatwaz.dadjokes.ui.util



fun emojiFor(reaction: Int): String = when (reaction) {
    1 -> "😐"
    2 -> "🙂"
    3 -> "😆"
    4 -> "🤣"
    5 -> "😭"
    else -> "❓"
}
