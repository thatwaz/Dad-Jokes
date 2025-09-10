package com.thatwaz.dadjokes.navigation


// NavRoutes.kt
data class ScreenRoute(val route: String)

object NavRoutes {
    object Intro {
        const val base = "intro"
        private const val argForce = "force"
        val routeWithArg = "$base?$argForce={$argForce}"
        fun route(force: Boolean = false): String =
            if (force) "$base?$argForce=$force" else base
    }

    val Home = ScreenRoute("home")
    val Saved = ScreenRoute("saved")
    val Rated = ScreenRoute("rated")
    val Settings = ScreenRoute("settings")
    val NotificationSettings = ScreenRoute("notification_settings")
    val PersonDetail = ScreenRoute("person/{person}")
    val AdPre = ScreenRoute("ad_pre")
    val AdPost = ScreenRoute("ad_post")
}






//object NavRoutes {
//    val Home = ScreenRoute("home")
//    val Saved = ScreenRoute("saved") // ✅ Renamed from Favorites
//    val Rated = ScreenRoute("rated")
//    val Settings = ScreenRoute("settings")
//    val NotificationSettings = ScreenRoute("notification_settings")
//    val PersonDetail = ScreenRoute("person/{person}") // arg
//}
//
//
//data class ScreenRoute(val route: String)



