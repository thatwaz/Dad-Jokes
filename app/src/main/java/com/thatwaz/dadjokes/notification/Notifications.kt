package com.thatwaz.dadjokes.notification

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat

// Single source of truth for your channel id/name/desc
const val CHANNEL_ID_DAILY = "daily_jokes"
private const val CHANNEL_NAME_DAILY = "Daily Dad Joke"
private const val CHANNEL_DESC_DAILY = "Daily joke notifications"

fun ensureDailyChannel(context: Context) {
    val channel = NotificationChannelCompat.Builder(
        CHANNEL_ID_DAILY,
        NotificationManagerCompat.IMPORTANCE_DEFAULT
    )
        .setName(CHANNEL_NAME_DAILY)
        .setDescription(CHANNEL_DESC_DAILY)
        .build()

    NotificationManagerCompat.from(context).createNotificationChannel(channel)
}


