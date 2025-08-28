package com.thatwaz.dadjokes

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.thatwaz.dadjokes.navigation.MainNavigation
import com.thatwaz.dadjokes.ui.theme.DadJokesTheme
import com.thatwaz.dadjokes.viewmodel.JokeViewModel
import com.thatwaz.dadjokes.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val jokeViewModel: JokeViewModel = hiltViewModel()
            val settingsViewModel: SettingsViewModel = hiltViewModel()

            val useDynamicColor by settingsViewModel.useDynamicColor.collectAsState()

            DadJokesTheme(
                dynamicColor = useDynamicColor,
                darkTheme = isSystemInDarkTheme()
            ) {
                // Optional disclaimer shown as a Toast-style message
                if (useDynamicColor && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    ShowDynamicColorWarning()
                }

                MainNavigation(viewModel = jokeViewModel)
            }
        }
    }
}

@Composable
fun ShowDynamicColorWarning() {
    Text(
        text = "Dynamic color is only supported on Android 12 and up.",
        // Optional styling can be added
    )
}


