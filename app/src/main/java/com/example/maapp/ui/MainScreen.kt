package com.example.maapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberSnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.maapp.LocalAppViewModel
import com.example.maapp.network.Player
import com.example.maapp.ui.screens.PlayerScreen
import com.example.maapp.ui.screens.SetupScreen
import com.example.maapp.ui.screens.SearchScreen
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text

@Composable
fun MainScreen(onNowPlayingChanged: (Player?) -> Unit) {
    val viewModel = LocalAppViewModel.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState: SnackbarHostState = rememberSnackbarHostState()
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.NowPlaying) }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(uiState.currentPlayerId, uiState.players) {
        onNowPlayingChanged(viewModel.currentPlayer())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.config == null) {
            SetupScreen(
                onSave = viewModel::saveServer,
                statusMessage = uiState.statusMessage,
                defaultUrl = uiState.config?.baseUrl ?: "http://192.168.0.190:8095"
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(selectedTabIndex = selectedTab.ordinal) {
                    Tab(
                        selected = selectedTab == MainTab.NowPlaying,
                        onClick = { selectedTab = MainTab.NowPlaying },
                        text = { Text("Now Playing") }
                    )
                    Tab(
                        selected = selectedTab == MainTab.Browse,
                        onClick = { selectedTab = MainTab.Browse },
                        text = { Text("Browse") }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        MainTab.NowPlaying -> PlayerScreen(
                            players = uiState.players,
                            selectedPlayerId = uiState.currentPlayerId,
                            queues = uiState.queues,
                            onPlayerSelected = viewModel::selectPlayer
                        )
                        MainTab.Browse -> SearchScreen(
                            query = uiState.searchQuery,
                            results = uiState.searchResults,
                            isSearching = uiState.isSearching,
                            onQueryChanged = viewModel::updateSearchQuery,
                            onSearch = viewModel::performSearch,
                            onAddToQueue = viewModel::addToQueue
                        )
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

private enum class MainTab { NowPlaying, Browse }
