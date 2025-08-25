package com.thatwaz.dadjokes.navigation


object NavRoutes {
    val Home = ScreenRoute("home")
    val Favorites = ScreenRoute("favorites")
    val Settings = ScreenRoute("settings")
    val NotificationSettings = ScreenRoute("notification_settings")
}

data class ScreenRoute(val route: String)


