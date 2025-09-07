package com.thatwaz.dadjokes.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.ZoneId

object DailyJokeScheduler {
    private const val REQ_CODE_DAILY = 10011
    private const val ACTION_DAILY_JOKE = "com.thatwaz.dadjokes.action.DAILY_JOKE"

    private const val EXTRA_HOUR = "extra_hour"
    private const val EXTRA_MIN = "extra_min"

    /** Schedule a one-shot alarm for the next occurrence of [time]. The receiver re-schedules daily. */
    @RequiresApi(Build.VERSION_CODES.O)
    fun schedule(context: Context, time: LocalTime) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val triggerAtMillis = nextTriggerAtMillis(time)
        val pi = pendingIntent(context, time)

        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
    }

    /** Cancel the scheduled daily joke (if any). */
    @RequiresApi(Build.VERSION_CODES.O)
    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, null))
    }

    /** Extract the chosen time from the Intent your BroadcastReceiver receives. */
    @RequiresApi(Build.VERSION_CODES.O)
    fun extrasToTime(intent: Intent): LocalTime {
        val h = intent.getIntExtra(EXTRA_HOUR, 9)
        val m = intent.getIntExtra(EXTRA_MIN, 0)
        return LocalTime.of(h, m)
    }

    // ---- internals ----

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pendingIntent(context: Context, time: LocalTime?): PendingIntent {
        val intent = Intent(context, DailyJokeReceiver::class.java).apply {
            action = ACTION_DAILY_JOKE
            // Keep the chosen time inside the intent so the receiver can re-schedule for tomorrow
            if (time != null) {
                putExtra(EXTRA_HOUR, time.hour)
                putExtra(EXTRA_MIN, time.minute)
            }
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, REQ_CODE_DAILY, intent, flags)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun nextTriggerAtMillis(time: LocalTime): Long {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        var next = now.withHour(time.hour).withMinute(time.minute)
            .withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return next.toInstant().toEpochMilli()
    }
}
