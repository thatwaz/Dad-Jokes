package com.thatwaz.dadjokes.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.thatwaz.dadjokes.navigation.NavRoutes

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomItem(NavRoutes.Home.route,     "Home",     Icons.Outlined.Home),
        BottomItem(NavRoutes.Saved.route,    "Saved",    Icons.Outlined.FavoriteBorder),
        BottomItem(NavRoutes.Rated.route,    "Rated",    Icons.Outlined.Star),
        BottomItem(NavRoutes.Settings.route, "Settings", Icons.Outlined.Settings),
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    // current route without any query/args suffixes
    val currentRouteBase = backStackEntry?.destination?.route?.substringBefore("?")

    NavigationBar {
        items.forEach { item ->
            val selected = currentRouteBase == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            // Pop up to Home (stable root for your bottom graph) and keep state
                            popUpTo(NavRoutes.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}





