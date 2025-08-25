package com.thatwaz.dadjokes.ui



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thatwaz.dadjokes.navigation.NavRoutes

@Composable
fun SettingsScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall
        )

        Button(
            onClick = {
                navController.navigate(NavRoutes.NotificationSettings.route)
            }
        ) {
            Text("Daily Joke Notification Settings")
        }

        // Add more settings toggles/switches here as needed later
    }
}
