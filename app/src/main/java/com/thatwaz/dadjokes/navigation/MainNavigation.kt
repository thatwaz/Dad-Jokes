package com.thatwaz.dadjokes.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thatwaz.dadjokes.ui.components.BottomNavBar
import com.thatwaz.dadjokes.ui.favorites.FavoritesScreen
import com.thatwaz.dadjokes.ui.home.HomeScreen
import com.thatwaz.dadjokes.viewmodel.JokeViewModel

@Composable
fun MainNavigation(viewModel: JokeViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavBar(navController) // 👈 using the new component
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
        }
    }
}

