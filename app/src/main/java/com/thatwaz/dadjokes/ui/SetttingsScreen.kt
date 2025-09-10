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
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AssistChip
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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
        snackbarHost = { SnackbarHost(hostState = snackbar) }
    ) { padding ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text("Settings", style = MaterialTheme.typography.headlineSmall) }

            // ——— Notifications ———
            item { Text("Notifications", style = MaterialTheme.typography.titleMedium) }

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
                                DailyJokeLocalNotifyWorker.enqueue(context)
                                scope.launch { snackbar.showSnackbar("Test notification sent") }
                            }
                        }
                        .padding(horizontal = 4.dp)
                )
            }

            item { HorizontalDivider() }

            // ——— Appearance ———
            item { Text("Appearance", style = MaterialTheme.typography.titleMedium) }

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
                    Spacer(Modifier.width(16.dp))
                    Switch(
                        checked = useDynamicColor,
                        onCheckedChange = { settingsViewModel.toggleDynamicColor() }
                    )
                }
            }

            item { HorizontalDivider() }

            // ——— Upgrades (Coming soon) ———
            item { Text("Upgrades", style = MaterialTheme.typography.titleMedium) }

            // Remove ads (coming soon)
            item {
                ComingSoonItem(
                    title = "Remove ads",
                    subtitle = "One-time purchase (coming soon)",
                    icon = Icons.Outlined.Block
                ) {
                    scope.launch {
                        snackbar.showSnackbar("Remove ads is coming soon in a future update.")
                    }
                }
            }

            // Rate this app (coming soon)
            item {
                ComingSoonItem(
                    title = "Rate this app",
                    subtitle = "Coming soon",
                    icon = Icons.Filled.StarRate
                ) {
                    scope.launch {
                        snackbar.showSnackbar("Ratings will be available once the app is on Google Play.")
                    }
                }
            }

            item { HorizontalDivider() }

            // ——— Contribute ———
            item { Text("Contribute", style = MaterialTheme.typography.titleMedium) }

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

            item {
                ListItem(
                    headlineContent = { Text("Share the app") },
                    supportingContent = { Text("With friends—or enemies if they hate puns!") },
                    leadingContent = { Icon(Icons.Outlined.Share, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { shareApp(context, playUrl = null) }
                        .padding(horizontal = 4.dp)
                )
            }

            // View intro again
            item {
                ListItem(
                    headlineContent = { Text("View intro / How it works") },
                    supportingContent = { Text("See the Sticklerz tour again") },
                    leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { navController.navigate(NavRoutes.Intro.route(force = true)) }
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}

/* ---------- Reusable Coming Soon row ---------- */
@Composable
private fun ComingSoonItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            AssistChip(onClick = {}, enabled = false, label = { Text("Coming soon") })
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .alpha(0.6f)                       // visually disabled
            .clickable { onClick() }           // tap shows snackbar with context
            .padding(horizontal = 4.dp)
    )
}




/* ---------- Helpers ---------- */

private const val DEV_EMAIL = "brettwaz23@gmail.com" // TODO: replace

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



