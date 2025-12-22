package com.example.maapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.maapp.data.PlayerRepository
import com.example.maapp.data.SettingsRepository
import com.example.maapp.network.MaNetworkModule
import com.example.maapp.network.Player
import com.example.maapp.network.PlayerQueue
import com.example.maapp.network.ServerConfig
import com.example.maapp.network.WebSocketUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val settingsRepository: SettingsRepository,
    private val networkModule: MaNetworkModule
) : ViewModel() {

    private val playerRepository = PlayerRepository(networkModule, viewModelScope)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        observeSettings()
        observePlayers()
        observeWebSocketEvents()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.serverConfig.collectLatest { config ->
                _uiState.update { it.copy(config = config) }
                config?.let {
                    playerRepository.connectSocket()
                    playerRepository.refreshFromHttp()
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.lastPlayerId.collectLatest { lastId ->
                _uiState.update { it.copy(currentPlayerId = lastId) }
            }
        }
    }

    private fun observePlayers() {
        viewModelScope.launch {
            playerRepository.players.collectLatest { players ->
                _uiState.update { state ->
                    val playerId = state.currentPlayerId ?: players.firstOrNull()?.playerId
                    state.copy(players = players, currentPlayerId = playerId)
                }
            }
        }
        viewModelScope.launch {
            playerRepository.queues.collectLatest { queues ->
                _uiState.update { it.copy(queues = queues) }
            }
        }
    }

    private fun observeWebSocketEvents() {
        viewModelScope.launch {
            networkModule.webSocketClient.events.collectLatest { event ->
                when (event) {
                    is WebSocketUpdate.Error -> _uiState.update { it.copy(statusMessage = event.message) }
                    is WebSocketUpdate.Connected -> _uiState.update { it.copy(statusMessage = "Connected to server") }
                    else -> Unit
                }
            }
        }
    }

    fun saveServer(url: String, token: String) {
        settingsRepository.saveServer(url.trim(), token.trim())
        _uiState.update { it.copy(statusMessage = "Saving server settings…") }
    }

    fun selectPlayer(player: Player) {
        settingsRepository.saveLastPlayer(player.playerId)
        _uiState.update { it.copy(currentPlayerId = player.playerId) }
    }

    fun currentPlayer(): Player? = uiState.value.players.firstOrNull { it.playerId == uiState.value.currentPlayerId }

    fun currentQueue(): PlayerQueue? = uiState.value.queues.firstOrNull { it.playerId == uiState.value.currentPlayerId }
}

class MainViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val networkModule: MaNetworkModule
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(settingsRepository, networkModule) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class UiState(
    val config: ServerConfig? = null,
    val players: List<Player> = emptyList(),
    val queues: List<PlayerQueue> = emptyList(),
    val currentPlayerId: String? = null,
    val statusMessage: String? = null
)
