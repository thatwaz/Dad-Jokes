package com.thatwaz.dadjokes.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.thatwaz.dadjokes.viewmodel.JokeViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationSettingsScreen(
    navController: NavController,
    viewModel: JokeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showTimePicker by remember { mutableStateOf(false) }
    var pendingTime: LocalTime? by remember { mutableStateOf(null) }

    val notificationTime by viewModel.notificationTime.collectAsState(initial = LocalTime.of(9, 0))
    val timeFmt = remember(notificationTime) {
        notificationTime.format(DateTimeFormatter.ofPattern("h:mm a"))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val t = pendingTime
        pendingTime = null
        if (granted && t != null) {
            ensureChannel(context)
            viewModel.saveNotificationTime(t)
            viewModel.scheduleDailyJokeNotification(context, t)
            val fmt = t.format(DateTimeFormatter.ofPattern("h:mm a"))
            scope.launch { snackbar.show("Daily joke set for around $fmt") }
        } else if (!granted) {
            scope.launch { snackbar.show("Notification permission denied") }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Daily Joke Notification", style = MaterialTheme.typography.titleLarge)
            Text("Current time: $timeFmt")

            Button(onClick = { showTimePicker = true }) {
                Text("Set Notification Time")
            }

            OutlinedButton(onClick = {
                ensureChannel(context)
                sendTestNotification(context)
                scope.launch { snackbar.show("Test notification sent") }
            }) {
                Text("Send test now")
            }

            // 👇 New heads-up that delivery is approximate
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Heads-up: delivery is approximate — Android may fire a few minutes before or after your chosen time to save battery.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (showTimePicker) {
                val now = LocalTime.now()
                TimePickerDialog(
                    context,
                    { _, hour: Int, minute: Int ->
                        val selected = LocalTime.of(hour, minute)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val granted = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                            if (!granted) {
                                pendingTime = selected
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                showTimePicker = false
                                return@TimePickerDialog
                            }
                        }

                        ensureChannel(context)
                        viewModel.saveNotificationTime(selected)
                        viewModel.scheduleDailyJokeNotification(context, selected)
                        showTimePicker = false

                        val fmt = selected.format(DateTimeFormatter.ofPattern("h:mm a"))
                        scope.launch { snackbar.show("Daily joke set for around $fmt") }
                    },
                    now.hour,
                    now.minute,
                    false
                ).show()
            }
        }
    }
}

/* ---------- helpers (unchanged, keep in same file or a utils file) ---------- */

private const val CHANNEL_ID_DAILY = "daily_jokes"

private fun ensureChannel(context: Context) {
    val channel = NotificationChannelCompat.Builder(
        CHANNEL_ID_DAILY,
        NotificationManagerCompat.IMPORTANCE_DEFAULT
    )
        .setName("Daily Dad Joke")
        .setDescription("Daily joke notifications")
        .build()
    NotificationManagerCompat.from(context).createNotificationChannel(channel)
}

suspend fun SnackbarHostState.show(message: String) {
    showSnackbar(message, withDismissAction = true)
}

fun sendTestNotification(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return
    }

    ensureChannel(context)

    val builder = androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID_DAILY)
        .setSmallIcon(com.thatwaz.dadjokes.R.drawable.ic_launcher_foreground) // TODO: your proper small icon
        .setContentTitle("DadJokes (test)")
        .setContentText("If you can see this, notifications are working.")
        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    NotificationManagerCompat.from(context).notify(9991, builder.build())
}

fun notificationsEnabled(context: Context): Boolean =
    NotificationManagerCompat.from(context).areNotificationsEnabled()

@RequiresApi(Build.VERSION_CODES.O)
fun openAppNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
    context.startActivity(intent)
}






