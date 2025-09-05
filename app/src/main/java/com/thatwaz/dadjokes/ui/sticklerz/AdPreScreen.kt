package com.thatwaz.dadjokes.ui.sticklerz

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thatwaz.dadjokes.navigation.NavRoutes
import com.thatwaz.dadjokes.ui.ads.InterstitialHolder


private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

@Composable
fun AdPreScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val activity = remember { context.findActivity() }

    // Interstitial for this flow
    val interstitial = remember { InterstitialHolder(context) }
    LaunchedEffect(Unit) { interstitial.load() }

    // Hide this screen while handing off to ad/navigation
    var transitioning by remember { mutableStateOf(false) }

    // 🔸 Pull a quip from the Interstitial-Ready pool with a 10-item cooldown
    val picker = remember { CooldownPickerByValue(window = 10) }
    val line = remember {
        val pool = Quips.poolFor(StickMood.InterstitialReady) // make sure Quips.poolFor(mood) exists
        picker.pick(pool, key = "interstitial")               // shared bucket for pre/post interstitial quips
    }

    // Optional: allow back unless we're already transitioning
    BackHandler(enabled = !transitioning) {
        navController.popBackStack()
    }

    // During handoff, render blank to avoid flashes
    if (transitioning) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        Text(
            text = line,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = {
                transitioning = true

                val goPost: () -> Unit = {
                    navController.navigate(NavRoutes.AdPost.route) {
                        launchSingleTop = true
                        // Prevent pre-screen flash on the way to post
                        popUpTo(NavRoutes.AdPre.route) { inclusive = true }
                    }
                }

                val act = activity
                if (act != null && interstitial.isReady()) {
                    interstitial.show(act) { goPost() }
                } else {
                    goPost()
                }
            }
        ) { Text("Continue") }

        TheaterStickmenImage(
            modifier = Modifier.fillMaxWidth(),
            heightDp = 240.dp
        )
    }
}






//package com.thatwaz.dadjokes.ui.sticklerz
//
//import android.app.Activity
//import android.content.Context
//import android.content.ContextWrapper
//import androidx.activity.compose.BackHandler
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Button
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import com.thatwaz.dadjokes.navigation.NavRoutes
//import com.thatwaz.dadjokes.ui.ads.InterstitialHolder
//
//private tailrec fun Context.findActivity(): Activity? =
//    when (this) {
//        is Activity -> this
//        is ContextWrapper -> baseContext.findActivity()
//        else -> null
//    }
//
///**
// * Full-screen “pre-roll” screen:
// * - Big theater PNG and a snarky line about the incoming ad
// * - Continue => shows interstitial (if ready), then navigates to AdPost
// * - Skip / Back => return to Home without showing ad
// */
//@Composable
//fun AdPreScreen(
//    navController: NavController
//) {
//    val context = LocalContext.current
//    val activity = remember { context.findActivity() }
//
//    // Load an interstitial for this flow
//    val interstitial = remember { InterstitialHolder(context) }
//    LaunchedEffect(Unit) { interstitial.load() }
//
//    // 👇 New: hide this screen the moment we start the ad / handoff
//    var transitioning by remember { mutableStateOf(false) }
//
//    val preQuips = remember {
//        listOf(
//            "Intermission warming up.",
//            "Brace yourselves…",
//            "Popcorn optional.",
//            "Large rectangle soon.",
//            "We preheated the capitalism.",
//            "Showtime (almost).",
//            "Everyone silence your conscience.",
//            "Rated E for Economy."
//        )
//    }
//    val line = remember { preQuips.random() }
//
//    BackHandler {
//        if (!transitioning) navController.popBackStack()
//        // if transitioning, swallow back to avoid visual pop
//    }
//
//    // 🔒 If we’re handing off to the ad/post, render a blank screen
//    if (transitioning) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(MaterialTheme.colorScheme.background)
//        )
//        return
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(horizontal = 16.dp, vertical = 20.dp),
//        verticalArrangement = Arrangement.SpaceBetween,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Spacer(Modifier.height(8.dp))
//        Text(
//            text = line,
//            style = MaterialTheme.typography.headlineSmall,
//            textAlign = TextAlign.Center
//        )
//
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            Button(
//                onClick = {
//                    // 🚦 Hide this UI before showing the interstitial
//                    transitioning = true
//
//                    val act = activity
//                    val goPost: () -> Unit = {
//                        navController.navigate(NavRoutes.AdPost.route) {
//                            launchSingleTop = true
//                            // 🧹 Remove AdPre so it can't flash on the way to AdPost
//                            popUpTo(NavRoutes.AdPre.route) { inclusive = true }
//                        }
//                    }
//
//                    if (act != null && interstitial.isReady()) {
//                        interstitial.show(act) {
//                            goPost()
//                        }
//                    } else {
//                        // Fallback if ad isn't ready
//                        goPost()
//                    }
//                }
//            ) { Text("Continue") }
//
//            Spacer(Modifier.height(8.dp))
//            TextButton(
//                enabled = !transitioning,
//                onClick = { navController.popBackStack() }
//            ) { Text("Skip") }
//        }
//
//        TheaterStickmenImage(
//            modifier = Modifier.fillMaxWidth(),
//            heightDp = 240.dp
//        )
//    }
//}



