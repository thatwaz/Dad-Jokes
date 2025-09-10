package com.thatwaz.dadjokes.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.thatwaz.dadjokes.ui.intro.IntroScreen
import com.thatwaz.dadjokes.ui.savedjokes.PersonDetailScreen
import com.thatwaz.dadjokes.ui.savedjokes.SavedScreen
import com.thatwaz.dadjokes.ui.sticklerz.AdPostScreen
import com.thatwaz.dadjokes.ui.sticklerz.AdPreScreen
import com.thatwaz.dadjokes.viewmodel.JokeViewModel
import com.thatwaz.dadjokes.viewmodel.OnboardingViewModel
import com.thatwaz.dadjokes.viewmodel.SettingsViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavigation(viewModel: JokeViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute !in setOf(
        NavRoutes.AdPre.route,
        NavRoutes.AdPost.route,
        NavRoutes.Intro.base,
        NavRoutes.Intro.routeWithArg
    )

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = { if (showBottomBar) BottomNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Intro.base, // ✅ start at base
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
        ) {
            composable(NavRoutes.Home.route) {
                HomeScreen(navController = navController, viewModel = viewModel)
            }
            composable(NavRoutes.Saved.route) { SavedScreen(navController) }
            composable(NavRoutes.Rated.route) { RatedJokesScreen(viewModel) }
            composable(NavRoutes.Settings.route) {
                val settingsVM: SettingsViewModel = hiltViewModel()
                SettingsScreen(navController, settingsVM)
            }
            composable(NavRoutes.NotificationSettings.route) {
                NotificationSettingsScreen(navController)
            }
            composable(
                route = NavRoutes.PersonDetail.route,
                arguments = listOf(navArgument("person") { type = NavType.StringType })
            ) { entry ->
                PersonDetailScreen(entry.arguments?.getString("person").orEmpty())
            }

            // ✅ Register Intro using the pattern route
            composable(
                route = NavRoutes.Intro.routeWithArg,
                arguments = listOf(
                    navArgument("force") { type = NavType.BoolType; defaultValue = false }
                )
            ) { entry ->
                val onboardingVM: OnboardingViewModel = hiltViewModel()
                val hasOnboarded by onboardingVM.hasOnboarded.collectAsState()
                val force = entry.arguments?.getBoolean("force") ?: false

                IntroScreen(
                    navController = navController,
                    onComplete = {
                        onboardingVM.complete()
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Intro.base) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    hasOnboarded = if (force) false else hasOnboarded
                )
            }

            composable(NavRoutes.AdPre.route) { AdPreScreen(navController) }
            composable(NavRoutes.AdPost.route) { AdPostScreen(navController) }
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



