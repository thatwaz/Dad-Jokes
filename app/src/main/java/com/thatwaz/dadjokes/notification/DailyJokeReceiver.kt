// com/thatwaz/dadjokes/notification/DailyJokeReceiver.kt
package com.thatwaz.dadjokes.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.thatwaz.dadjokes.worker.DailyJokeLocalNotifyWorker

class DailyJokeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Show the notification via WorkManager
        DailyJokeLocalNotifyWorker.enqueue(context)

        // Reschedule tomorrow at the chosen time
        val chosen = DailyJokeScheduler.extrasToTime(intent)
        DailyJokeScheduler.schedule(context, chosen)
    }
}


