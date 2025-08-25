package com.thatwaz.dadjokes.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DailyJokeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Fetch joke (from repo or API)
        // Build and send notification
        JokeNotificationHelper.showNotification(context, "Your daily dad joke is ready!")
    }
}
