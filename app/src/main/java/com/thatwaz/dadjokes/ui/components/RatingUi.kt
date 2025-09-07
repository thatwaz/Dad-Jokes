package com.thatwaz.dadjokes.ui.components



object RatingUi {
    fun emojiFor(rating: Int): String = when (rating) {
        1 -> "😒"
        2 -> "😐"
        3 -> "🙂"
        4 -> "😆"
        5 -> "😂"
        else -> ""
    }
}
