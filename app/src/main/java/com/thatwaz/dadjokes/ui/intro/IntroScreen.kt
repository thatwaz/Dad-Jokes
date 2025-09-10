package com.thatwaz.dadjokes.ui.intro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thatwaz.dadjokes.R
import com.thatwaz.dadjokes.navigation.NavRoutes
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun IntroScreen(
    navController: NavController,
    onComplete: () -> Unit,
    hasOnboarded: Boolean
) {
    // If they’ve already seen it, skip immediately
    LaunchedEffect(hasOnboarded) {
        if (hasOnboarded) {
            navController.navigate(NavRoutes.Home.route) {
                popUpTo(NavRoutes.Intro.base) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    if (hasOnboarded) return

    val pager = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    BackHandler {
        if (pager.currentPage > 0) {
            scope.launch { pager.animateScrollToPage(pager.currentPage - 1) }
        } else {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    if (pager.currentPage > 0) {
                        IconButton(onClick = {
                            scope.launch { pager.animateScrollToPage(pager.currentPage - 1) }
                        }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                    }
                },
                actions = { TextButton(onClick = onComplete) { Text("Skip") } }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 20.dp)
                .systemBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // PAGES
            HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { page ->
                when (page) {
                    0 -> IntroPage(
                        title = "Dad Jokes Vault",
                        subtitle = "Groans, giggles, and classic dad humor.",
                        bullets = listOf(
                            "Fresh jokes with tap-to-reveal punchlines",
                            "Save favorites and share the best (or worst) ones",
                            "Rate jokes to train your Vault"
                        )
                    )
                    1 -> IntroPage(
                        title = "Meet the Dads",
                        subtitle = "Two armchair critics who riff on ads.",
                        bullets = listOf(
                            "Banner at the bottom is their ‘screen’",
                            "They poke quips at ads so you don’t have to",
                            "Interstitials every few jokes (Remove ads — coming soon)"
                        )
                    )
                    else -> IntroPage(
                        title = "You’re in control",
                        subtitle = "Keep notifications optional, submit jokes, send ad quips.",
                        bullets = listOf(
                            "Daily joke notification (optional in Settings)",
                            "Submit jokes to icanhazdadjoke.com",
                            "Send us your sarcastic ad quips"
                        )
                    )
                }
            }

            // THEATER ART (PNG)
            Image(
                painter = painterResource(R.drawable.intro_dads_theater),
                contentDescription = stringResource(R.string.cd_intro_dads),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(bottom = 12.dp),
                // Tint line art so it shows on light *and* dark backgrounds
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )

            // CONTROLS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Dots
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { i ->
                        val active = i == pager.currentPage
                        Box(
                            Modifier
                                .size(if (active) 10.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (active) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                )
                        )
                    }
                }

                Button(
                    onClick = {
                        if (pager.currentPage < 2) {
                            scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                        } else {
                            onComplete()
                        }
                    }
                ) { Text(if (pager.currentPage < 2) "Next" else "Start jokin’") }
            }
        }
    }
}

@Composable
private fun IntroPage(
    title: String,
    subtitle: String,
    bullets: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(0.94f)
        ) {
            bullets.forEach { b -> Text("• $b", style = MaterialTheme.typography.bodyLarge) }
        }
    }
}



