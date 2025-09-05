package com.thatwaz.dadjokes.ui.sticklerz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thatwaz.dadjokes.navigation.NavRoutes

/**
 * Full-screen “post-roll” screen:
 * - Big theater PNG and a quip about the ad you just saw
 * - “Back to jokes” => pop to Home and signal it to fetch a new joke
 */
@Composable
fun AdPostScreen(
    navController: NavController
) {
    // Pick a post-interstitial quip with a 10-item cooldown (shared with AdPre)
    val picker = remember { CooldownPickerByValue(window = 10) }
    val line = remember {
        val pool = Quips.poolFor(StickMood.InterstitialDismissed)
        picker.pick(pool, key = "interstitial")
    }

    fun continueToHome() {
        // Signal Home to fetch a fresh joke after returning
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.set("afterAdFetch", true)

        // Pop back to Home (do not remove Home)
        navController.popBackStack(NavRoutes.Home.route, inclusive = false)
    }

    // Back goes back to jokes
    BackHandler { continueToHome() }

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

        Button(onClick = { continueToHome() }) {
            Text("Back to jokes")
        }

        TheaterStickmenImage(
            modifier = Modifier.fillMaxWidth(),
            heightDp = 240.dp
        )
    }
}





//package com.thatwaz.dadjokes.ui.sticklerz
//
//import androidx.activity.compose.BackHandler
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Button
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import com.thatwaz.dadjokes.navigation.NavRoutes
//
///**
// * Full-screen “post-roll” screen:
// * - Big theater PNG and a quip about the ad you just saw
// * - “Back to jokes” => pop to Home and signal it to fetch a new joke
// */
//@Composable
//fun AdPostScreen(
//    navController: NavController
//) {
//    val postQuips = remember {
//        listOf(
//            "You survived!",
//            "Back to jokes!",
//            "And we’re clear.",
//            "Intermission over.",
//            "Thanks for enduring.",
//            "Cue the punchlines.",
//            "Wallet status: holding.",
//            "We now return to your scheduled groans."
//        )
//    }
//    val line = remember { postQuips.random() }
//
//    fun continueToHome() {
//        // Signal Home to fetch a new joke
//        navController.previousBackStackEntry
//            ?.savedStateHandle
//            ?.set("afterAdFetch", true)
//
//        // Pop back to Home
//        navController.popBackStack(NavRoutes.Home.route, inclusive = false)
//    }
//
//    BackHandler { continueToHome() }
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
//        Button(onClick = { continueToHome() }) {
//            Text("Back to jokes")
//        }
//
//        TheaterStickmenImage(
//            modifier = Modifier.fillMaxWidth(),
//            heightDp = 240.dp
//        )
//    }
//}


