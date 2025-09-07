package com.thatwaz.dadjokes.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thatwaz.dadjokes.R
import com.thatwaz.dadjokes.navigation.NavRoutes
import com.thatwaz.dadjokes.viewmodel.SettingsViewModel
import com.thatwaz.dadjokes.worker.DailyJokeLocalNotifyWorker
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val useDynamicColor by settingsViewModel.useDynamicColor.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        // Scrollable list with proper content padding
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)            // ← from Scaffold
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Settings", style = MaterialTheme.typography.headlineSmall)
            }

            // ——— Notifications ———
            item {
                Text("Notifications", style = MaterialTheme.typography.titleMedium)
            }

            // Daily joke settings
            item {
                ListItem(
                    headlineContent = { Text("Daily Joke Notification Settings") },
                    supportingContent = { Text("Pick a time for your daily groan") },
                    leadingContent = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { navController.navigate(NavRoutes.NotificationSettings.route) }
                        .padding(horizontal = 4.dp)
                )
            }

            // Send a test notification (and prompt to enable if disabled)
            item {
                ListItem(
                    headlineContent = { Text("Send a test notification") },
                    supportingContent = { Text("Make sure notifications are working") },
                    leadingContent = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (!notificationsEnabled(context)) {
                                openAppNotificationSettings(context)
                                scope.launch { snackbar.showSnackbar("Enable notifications for DadJokes") }
                            } else {
                                // Sends our local, hard-coded pool joke as a notification
                                DailyJokeLocalNotifyWorker.enqueue(context)
                                scope.launch { snackbar.showSnackbar("Test notification sent") }
                            }
                        }
                        .padding(horizontal = 4.dp)
                )
            }

            item { HorizontalDivider() }

            // ——— Appearance ———
            item {
                Text("Appearance", style = MaterialTheme.typography.titleMedium)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Use Dynamic Color", style = MaterialTheme.typography.bodyLarge)
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                            Text(
                                text = "Dynamic color is only available on Android 12 and above.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp)) // breathing room from the switch
                    Switch(
                        checked = useDynamicColor,
                        onCheckedChange = { settingsViewModel.toggleDynamicColor() }
                    )
                }
            }

            item { HorizontalDivider() }

            // ——— Contribute ———
            item {
                Text("Contribute", style = MaterialTheme.typography.titleMedium)
            }

            // Submit a joke (opens icanhazdadjoke.com/submit)
            item {
                ListItem(
                    headlineContent = { Text("Submit a joke") },
                    supportingContent = { Text("Opens icanhazdadjoke.com/submit") },
                    leadingContent = { Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { openCustomTab(context, "https://icanhazdadjoke.com/submit") }
                        .padding(horizontal = 4.dp)
                )
            }

            // Send us an ad quip (email)
            item {
                ListItem(
                    headlineContent = { Text("Send us an ad quip") },
                    supportingContent = { Text("Email your sarcastic banner/interstitial ideas") },
                    leadingContent = { Icon(Icons.Outlined.Email, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { sendQuipEmail(context) }
                        .padding(horizontal = 4.dp)
                )
            }

            // Share the app (friends… or enemies 😈)
            item {
                ListItem(
                    headlineContent = { Text("Share the app") },
                    supportingContent = { Text("With friends—or enemies if they hate puns!") },
                    leadingContent = { Icon(Icons.Outlined.Share, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { shareApp(context, playUrl = null) } // add Play URL later
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}



/* ---------- Helpers ---------- */

private const val DEV_EMAIL = "you@example.com" // TODO: replace

private fun openCustomTab(context: Context, url: String) {
    try {
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(context, Uri.parse(url))
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

private fun sendQuipEmail(context: Context) {
    val subject = "DadJokes – Ad Quip Suggestion"
    val body = """
        Got a quip idea? Drop it below 👇
        
        • Banner quip:
        
        • Interstitial (pre-ad) quip:
        
        • Interstitial (post-ad) quip:
        
        (Optional) Name / credit:
    """.trimIndent()

    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(DEV_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    try {
        context.startActivity(emailIntent)
    } catch (_: ActivityNotFoundException) {
        val chooser = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(DEV_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(Intent.createChooser(chooser, "Send email"))
    }
}

private fun shareApp(context: Context, playUrl: String? = null) {
    val appName = try {
        context.getString(R.string.app_name)
    } catch (_: Exception) {
        "DadJokes"
    }

    // if you eventually publish, set playUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"
    val message = buildString {
        append("Check out $appName — groan-worthy dad jokes + sarcastic stick-figure commentary. ")
        append("Share with friends (or enemies if they hate puns) 😂")
        if (playUrl != null) {
            append("\n\n$playUrl")
        } else {
            append("\n\n(PS: Not on Play yet—I'll send you the APK/link.)")
        }
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(intent, "Share $appName"))
}



