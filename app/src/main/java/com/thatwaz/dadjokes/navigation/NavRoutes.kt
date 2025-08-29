package com.thatwaz.dadjokes.navigation


object NavRoutes {
    val Home = ScreenRoute("home")
    val Saved = ScreenRoute("saved") // ✅ Renamed from Favorites
    val Rated = ScreenRoute("rated")
    val Settings = ScreenRoute("settings")
    val NotificationSettings = ScreenRoute("notification_settings")
    val PersonDetail = ScreenRoute("person/{person}") // arg
}


data class ScreenRoute(val route: String)



