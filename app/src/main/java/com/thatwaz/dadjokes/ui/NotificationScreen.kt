package com.thatwaz.dadjokes.ui

import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.thatwaz.dadjokes.viewmodel.JokeViewModel
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
    var showTimePicker by remember { mutableStateOf(false) }
    val notificationTime by viewModel.notificationTime.collectAsState(initial = LocalTime.of(9, 0))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {}, // Optional actions
            )
        }
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

            Text("Current time: ${notificationTime.format(DateTimeFormatter.ofPattern("h:mm a"))}")

            Button(onClick = { showTimePicker = true }) {
                Text("Set Notification Time")
            }

            if (showTimePicker) {
                val currentTime = LocalTime.now()
                TimePickerDialog(
                    context,
                    { _, hour: Int, minute: Int ->
                        val selectedTime = LocalTime.of(hour, minute)
                        viewModel.saveNotificationTime(selectedTime)
                        viewModel.scheduleDailyJokeNotification(context, selectedTime)
                        showTimePicker = false
                    },
                    currentTime.hour,
                    currentTime.minute,
                    false
                ).show()
            }
        }
    }
}



