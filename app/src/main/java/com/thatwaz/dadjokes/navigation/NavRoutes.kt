package com.thatwaz.dadjokes.navigation


object NavRoutes {
    val Home = ScreenRoute("home")
    val Saved = ScreenRoute("saved") // ✅ Renamed from Favorites
    val Rated = ScreenRoute("rated")
    val Settings = ScreenRoute("settings")
    val NotificationSettings = ScreenRoute("notification_settings")

    // With arg
    private const val PERSON_ARG = "person"
    val PersonDetail = ScreenRoute("person/{$PERSON_ARG}")
    fun personDetail(person: String) = ScreenRoute("person/$person")

    // 🔸 New: interstitial flow screens
    val AdPre = ScreenRoute("ad_pre")     // full-screen “ad incoming” theater
    val AdPost = ScreenRoute("ad_post")   // full-screen “post-ad” theater
}

data class ScreenRoute(val route: String)


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



