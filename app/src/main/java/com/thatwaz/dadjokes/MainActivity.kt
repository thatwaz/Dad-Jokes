package com.thatwaz.dadjokes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.hilt.navigation.compose.hiltViewModel
import com.thatwaz.dadjokes.navigation.MainNavigation
import com.thatwaz.dadjokes.viewmodel.JokeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: JokeViewModel = hiltViewModel()
            MaterialTheme {
                MainNavigation(viewModel)
            }
        }
    }
}

