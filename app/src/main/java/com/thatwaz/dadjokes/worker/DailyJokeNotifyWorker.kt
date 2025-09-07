package com.thatwaz.dadjokes.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.thatwaz.dadjokes.MainActivity
import com.thatwaz.dadjokes.R
import com.thatwaz.dadjokes.notification.CHANNEL_ID_DAILY
import com.thatwaz.dadjokes.notification.NotificationPool
import com.thatwaz.dadjokes.notification.ensureDailyChannel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DailyJokeLocalNotifyWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // Android 13+ permission gate
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                appContext, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return Result.success()
        }

        ensureDailyChannel(appContext)

        val joke = NotificationPool.next(appContext)
        val (title, bigText) = joke.split("\n\n").let {
            if (it.size >= 2) "Daily Dad Joke" to joke
            else "Daily Dad Joke" to it.first()
        }

        // Open app when tapped
        val intent = Intent(appContext, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val contentIntent = PendingIntent.getActivity(
            appContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(appContext, CHANNEL_ID_DAILY)
            .setSmallIcon(R.drawable.ic_launcher_foreground)   // 24dp white vector in res/drawable
            .setContentTitle(title)
            .setContentText(bigText.take(60))           // short line for collapsed view
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(appContext).notify(9992, notif)
        return Result.success()
    }

    companion object {
        fun enqueue(context: Context) {
            val req = OneTimeWorkRequestBuilder<DailyJokeLocalNotifyWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            WorkManager.getInstance(context).enqueue(req)
        }
    }
}
