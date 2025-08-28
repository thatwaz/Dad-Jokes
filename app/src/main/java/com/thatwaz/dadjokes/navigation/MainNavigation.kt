package com.thatwaz.dadjokes.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thatwaz.dadjokes.ui.NotificationSettingsScreen
import com.thatwaz.dadjokes.ui.RatedJokesScreen
import com.thatwaz.dadjokes.ui.SettingsScreen
import com.thatwaz.dadjokes.ui.components.BottomNavBar
import com.thatwaz.dadjokes.ui.favorites.FavoritesScreen
import com.thatwaz.dadjokes.ui.home.HomeScreen
import com.thatwaz.dadjokes.viewmodel.JokeViewModel
import com.thatwaz.dadjokes.viewmodel.SettingsViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavigation(viewModel: JokeViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.Home.route) {
                HomeScreen(viewModel)
            }
            composable(NavRoutes.Favorites.route) {
                FavoritesScreen(viewModel)
            }
            composable(NavRoutes.Rated.route) {
                RatedJokesScreen(viewModel)
            }
            composable(NavRoutes.Settings.route) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(navController, settingsViewModel)
            }
            composable(NavRoutes.NotificationSettings.route) {
                NotificationSettingsScreen(navController)
            }

        }
    }
}



