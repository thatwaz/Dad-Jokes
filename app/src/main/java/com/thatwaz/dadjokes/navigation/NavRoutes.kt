package com.thatwaz.dadjokes.navigation


sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("home")
    object Favorites : NavRoutes("favorites")
}
