package com.thatwaz.dadjokes.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.thatwaz.dadjokes.ui.NotificationSettingsScreen
import com.thatwaz.dadjokes.ui.RatedJokesScreen
import com.thatwaz.dadjokes.ui.SettingsScreen
import com.thatwaz.dadjokes.ui.components.BottomNavBar

import com.thatwaz.dadjokes.ui.home.HomeScreen
import com.thatwaz.dadjokes.ui.savedjokes.PersonDetailScreen
import com.thatwaz.dadjokes.ui.savedjokes.SavedScreen
import com.thatwaz.dadjokes.ui.sticklerz.AdPostScreen
import com.thatwaz.dadjokes.ui.sticklerz.AdPreScreen
import com.thatwaz.dadjokes.viewmodel.JokeViewModel
import com.thatwaz.dadjokes.viewmodel.SettingsViewModel
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavigation(viewModel: JokeViewModel) {
    val navController = rememberNavController()

    // Hide bottom bar on the full-screen ad flow screens
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute !in setOf(
        NavRoutes.AdPre.route,
        NavRoutes.AdPost.route
    )

    Scaffold(
        bottomBar = { if (showBottomBar) BottomNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.Home.route) {
                // ⬇️ pass navController so Home can navigate to AdPre on the 5th joke
                HomeScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }

            composable(NavRoutes.Saved.route) {
                SavedScreen(navController)
            }

            composable(
                route = NavRoutes.PersonDetail.route, // "person/{person}"
                arguments = listOf(navArgument("person") { type = NavType.StringType })
            ) { backStackEntry ->
                val person = backStackEntry.arguments?.getString("person").orEmpty()
                PersonDetailScreen(person = person)
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

            // 🔸 New full-screen ad flow screens
            composable(NavRoutes.AdPre.route) {
                AdPreScreen(navController = navController)
            }
            composable(NavRoutes.AdPost.route) {
                AdPostScreen(navController = navController)
            }
        }
    }
}
//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//fun MainNavigation(viewModel: JokeViewModel) {
//    val navController = rememberNavController()
//
//    Scaffold(
//        bottomBar = { BottomNavBar(navController) }
//    ) { innerPadding ->
//        NavHost(
//            navController = navController,
//            startDestination = NavRoutes.Home.route,
//            modifier = Modifier.padding(innerPadding)
//        ) {
//            composable(NavRoutes.Home.route) {
//                HomeScreen(viewModel)
//            }
//
//            composable(NavRoutes.Saved.route) {
//                SavedScreen(navController)   // ← pass the one from NavHost
//            }
//
//
//            composable(
//                route = NavRoutes.PersonDetail.route, // "person/{person}"
//                arguments = listOf(navArgument("person") { type = NavType.StringType })
//            ) { backStackEntry ->
//                val person = backStackEntry.arguments?.getString("person").orEmpty()
//                PersonDetailScreen(person = person)
//            }
//
//            composable(NavRoutes.Rated.route) {
//                RatedJokesScreen(viewModel)
//            }
//
//            composable(NavRoutes.Settings.route) {
//                val settingsViewModel: SettingsViewModel = hiltViewModel()
//                SettingsScreen(navController, settingsViewModel)
//            }
//
//            composable(NavRoutes.NotificationSettings.route) {
//                NotificationSettingsScreen(navController)
//            }
//        }
//    }
//}



