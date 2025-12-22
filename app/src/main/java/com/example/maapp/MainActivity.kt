package com.example.maapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.view.WindowCompat
import com.example.maapp.player.MediaSessionManager
import com.example.maapp.ui.MainScreen
import com.example.maapp.ui.MainViewModel
import com.example.maapp.ui.MainViewModelFactory
import com.example.maapp.ui.theme.MaTheme

val LocalAppViewModel = staticCompositionLocalOf<MainViewModel> {
    error("MainViewModel not provided")
}

class MainActivity : ComponentActivity() {

    private lateinit var mediaSessionManager: MediaSessionManager
    private val viewModel: MainViewModel by viewModels {
        val app = application as MaApp
        MainViewModelFactory(app.settingsRepository, app.networkModule)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        mediaSessionManager = MediaSessionManager(this)

        setContent {
            MaTheme {
                CompositionLocalProvider(LocalAppViewModel provides viewModel) {
                    MainScreen(onNowPlayingChanged = mediaSessionManager::updateFromPlayer)
                }
            }
        }
    }

    override fun onDestroy() {
        mediaSessionManager.release()
        super.onDestroy()
    }
}
